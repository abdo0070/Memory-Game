package com.abdalla_mmdouh.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.abdalla_mmdouh.myapplication.models.BoardSize
import com.abdalla_mmdouh.myapplication.models.MemoryGame
import com.abdalla_mmdouh.myapplication.models.UserImageList
import com.github.jinatonic.confetti.CommonConfetti
import com.github.jinatonic.confetti.confetto.Confetto
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.download_board.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter: MemoryBoardAdapter
    lateinit var memoryGame : MemoryGame
    lateinit var clRoot : ConstraintLayout
    val REQUEST_CODE = 100
    private var customGameImages : List<String>? = null
    val TAG = "MainActivity"
    private val db = Firebase.firestore
    private var gameName : String? = null
    var boardSize : BoardSize = BoardSize.EASY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        game()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu , menu)
        return super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null){
                Log.i("customGameName" , "null name ")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        db.collection(GAME_DEC).document(customGameName).get().addOnSuccessListener { document ->

            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null){
                Log.i("TAG" , "Failed to Get Images")
                Snackbar.make(clRoot,"Sorry We couldn't find this game",Snackbar.LENGTH_SHORT).show()

                return@addOnSuccessListener
            }
            Log.i("TAG" , "Failed  of imges : ${customGameImages?.size} and board size : $boardSize")
            val numCards = userImageList.images.size * 2
            customGameImages = userImageList.images
            boardSize = BoardSize.getByValue(numCards)
            gameName = customGameName
            for (imageUrl in userImageList.images){
                // to Save it Picasso  cash
                Picasso.get().load(imageUrl).fetch()
            }
            Toast.makeText(this@MainActivity,"You are now playing a $gameName game",Toast.LENGTH_SHORT).show()
            game()
        }.addOnFailureListener{
            Log.i("TAG" , "Failed to Get Images")
        }
    }

    private fun game()
    {
        supportActionBar?.title = gameName ?:getString(R.string.app_name)
        when (boardSize)
        {
            BoardSize.EASY -> {
                tvMoves.text = "0"
                tvPair.text = "0/4"
            }
            BoardSize.MEDIUM -> {
                tvMoves.text = "0"
                tvPair.text = "0/9"
            }
            BoardSize.HARD -> {
                tvMoves.text = "0"
                tvPair.text = "0/12"
            }
        }
        memoryGame = MemoryGame(boardSize , customGameImages)
        adapter = MemoryBoardAdapter(this , boardSize , memoryGame.cards , object : MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
               memoryGame.isFLipped(position)
                upDateGame()
                tvPair.text = "${memoryGame.numOfPairs} / ${boardSize.getPairs()}"
                tvMoves.text = "${memoryGame.numOfMoves}"

            }
        })
        board()
    }
    private fun board ()
    {
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this ,boardSize.getWidth())
    }
    private fun upDateGame()
    {
        if (memoryGame.winGame())
        {
            clRoot= findViewById(R.id.clRoot)
            Snackbar.make(clRoot , "You Win The Game With Score ${memoryGame.numOfMoves}." , Snackbar.LENGTH_SHORT).show()
            CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.MAGENTA,Color.RED,Color.BLUE,Color.YELLOW)).oneShot()
        }
        adapter.notifyDataSetChanged()
    }
    private fun customDialog() {
        var customSize = LayoutInflater.from(this@MainActivity).inflate(R.layout.board_size,null)
        showAlertDialog("Choose Level",customSize,View.OnClickListener{
            boardSize = when(customSize.findViewById<RadioGroup>(R.id.rgLevels).checkedRadioButtonId)
            {
                R.id.rbEASY -> BoardSize.EASY
                R.id.rbMEDIUM -> BoardSize.MEDIUM
                else -> {BoardSize.HARD}
            }
            // Start a new Activity
            val intent = Intent(this@MainActivity , CreateActivity::class.java).also {
                it.putExtra(CUSOM_BOARD_SIZE , boardSize)
                startActivityForResult(it,REQUEST_CODE)
            }

        })
    }

    private fun showLevelsDialog() {
        var boardNewSize= LayoutInflater.from(this@MainActivity).inflate(R.layout.board_size , null)
        val radioGroup = boardNewSize.findViewById<RadioGroup>(R.id.rgLevels)
        showAlertDialog("Choose Level",boardNewSize,View.OnClickListener {
            boardSize =  when (radioGroup.checkedRadioButtonId)
            {
                R.id.rbEASY -> BoardSize.EASY
                R.id.rbMEDIUM -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            board()
        })
    }

    private fun showAlertDialog(title : String ,view : View? ,positiveClickListener : View.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel" , null)
            .setPositiveButton("Ok"){
                    _,_ -> positiveClickListener.onClick(null)
            }.show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.miRefresh ->{
                // this to reset the whole game
                if (!memoryGame.winGame())
                showAlertDialog( "Are You Sure To Restart The Game",null,View.OnClickListener { game()  })
                else {game()}
            }
            R.id.miChooseAnotherLevel -> {
                showLevelsDialog()
            }
            R.id.miCustomLevels -> {
                customDialog()
            }
            R.id.miDownloadGame ->{
                enterDownloadGameName()
            }
        }

        return super.onOptionsItemSelected(item)
        return true
    }

    private fun enterDownloadGameName() {
        val boardDownload =LayoutInflater.from(this@MainActivity).inflate(R.layout.download_board , null)
        showAlertDialog("Enter Game Name ",boardDownload,View.OnClickListener{
            val etGame = boardDownload.findViewById<EditText>(R.id.etDownloadGameName)
            val gameDownloadName = etGame.text.toString()
            downloadGame(gameDownloadName)
        })
    }
}
