package com.abdalla_mmdouh.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.abdalla_mmdouh.myapplication.models.BitMapScaler
import com.abdalla_mmdouh.myapplication.models.BoardSize
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_custom.*
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    companion object{
    private const val TAG= "CreateActivity"
    private  const val SELECT_PHOTO_CODE = 210
        private const val READ_EXTERNAL_PHOTO_CODE = 22
        private const val PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_LENGTH = 3
        private const val MAX_GAME_LENGTH = 10

    }
    lateinit var boardSize: BoardSize
    private var selectedImages = mutableListOf<Uri>()
    var numImageOfLevel = 0
    lateinit var adapter : CustomMemoryGameAdapter
    private  val storge = Firebase.storage
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)
        board()
        save()
        gameText()

    }
    private fun gameText(){
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))
        etGameName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldButtonEnabled()
            }
        })
    }

    private fun board(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(CUSOM_BOARD_SIZE) as BoardSize
        numImageOfLevel = boardSize.getPairs()
        supportActionBar?.title= "Choose pics 0/$numImageOfLevel"
        adapter = CustomMemoryGameAdapter(this@CreateActivity , selectedImages , boardSize ,object : CustomMemoryGameAdapter.ImageClickListener{
            override fun selectPhoto() {
                if(permissionGranted(this@CreateActivity , PHOTO_PERMISSION)){
                    startPhotoIntent()
                }
                else
                {
                    // ask the user to accept the permission
                    requestPermission(this@CreateActivity,PHOTO_PERMISSION,READ_EXTERNAL_PHOTO_CODE)
                }
            }
        })

        rvCustom.adapter = adapter
        rvCustom.setHasFixedSize(true)
        rvCustom.layoutManager = GridLayoutManager(this@CreateActivity , boardSize.getWidth())
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTO_CODE){
            if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startPhotoIntent()
            }
            else{
              Toast.makeText(this@CreateActivity , "to make custom game accept the permission" , Toast.LENGTH_LONG).show()
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            android.R.id.home -> finish()
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO_CODE || data != null || resultCode != Activity.RESULT_OK) {
            val date = data?.data
            val clipData = data?.clipData
            // here we wanna store that photos
            if (clipData != null){
            for (i in 0 until clipData.itemCount){
                val clipitem = clipData.getItemAt(i)
                if (selectedImages.size < numImageOfLevel){
                    selectedImages.add(clipitem.uri)
                }
            }
        }else if (data != null){
                if (selectedImages.size < numImageOfLevel){
                    selectedImages.add(date!!)
                }
            }
            }
        supportActionBar?.title = "you picked (${selectedImages.size} / $numImageOfLevel"
            adapter.notifyDataSetChanged()
            btnSave.isEnabled = shouldButtonEnabled()
    }
    private fun startPhotoIntent() {
        val  intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE , true)
        startActivityForResult(Intent.createChooser(intent , "Choose photos") , SELECT_PHOTO_CODE )
    }
    private fun save(){
        btnSave.setOnClickListener {
            // Save Data To FireBase
            saveDataToFirebase()
        }
    }
    private fun shouldButtonEnabled() : Boolean{
        if (selectedImages.size != numImageOfLevel){
            return false
        }
        if (etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_LENGTH){
            return false
        }
        return true
    }

   private fun saveDataToFirebase(){
       val customGameName = etGameName.text.toString()
       btnSave.isEnabled = false
       (db.collection(GAME_DEC).document(customGameName)).get().addOnSuccessListener { documents ->
           if (customGameName != null && documents.data != null){
               AlertDialog.Builder(this@CreateActivity)
                   .setTitle("Already Token")
                   .setMessage("The Name is Already Exit Choose Another One")
                   .setPositiveButton("Ok" , null)
                   .show()
                   btnSave.isEnabled = true
           }
           else{
               hanleUploadImages(customGameName)
           }
       }.addOnFailureListener{
           Toast.makeText(this@CreateActivity , "Error while Saving The Game Try Again" , Toast.LENGTH_SHORT).show()
           btnSave.isEnabled = true
       }
    }

    private fun hanleUploadImages(customGameName: String) {
        var didErorr = false
        pbUploading.visibility = View.VISIBLE
        val uploadedPhotoUrl = mutableListOf<String>()
        for ((index , photoUri) in selectedImages.withIndex()){
            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "images/$customGameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference =  storge.reference.child(filePath)
            photoReference.putBytes(imageByteArray)
                .continueWithTask{
                        photoUploadTask -> Log.i(TAG , "Upladed byte ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener{
                        downlodaTask ->
                    if (!downlodaTask.isSuccessful){
                        Log.e(TAG , "Exception with FireBase storage",downlodaTask.exception)
                        Toast.makeText(this@CreateActivity , "Failed to upload the photo",Toast.LENGTH_SHORT).show()
                        didErorr = true
                        return@addOnCompleteListener
                    }
                    if(didErorr){
                        return@addOnCompleteListener
                        pbUploading.visibility = View.GONE
                    }
                    val photoUrl = downlodaTask.result.toString()
                    uploadedPhotoUrl.add(photoUrl)
                    pbUploading.progress = uploadedPhotoUrl.size * 100 / selectedImages.size
                    Log.i(TAG , "uploaded image $photoUrl , num of photo :${uploadedPhotoUrl.size}")
                    if (uploadedPhotoUrl.size == selectedImages.size){
                        handledAllImagesUploaded(customGameName , uploadedPhotoUrl)
                    }

                }
        }
    }

    private fun handledAllImagesUploaded(gameName: String, imageUrl: MutableList<String>) {
        db.collection(GAME_DEC).document(gameName)
            .set(mapOf("images" to imageUrl))
            .addOnCompleteListener{
                gameCreationTask ->
                pbUploading.visibility = View.GONE
                if (!gameCreationTask.isSuccessful){
                    Toast.makeText(this@CreateActivity,"Failed game Creation" , Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                AlertDialog.Builder(this@CreateActivity)
                    .setTitle("Uploaded complete lets play your game : ${gameName}")
                    .setPositiveButton("Ok"){
                        _,_ ->
                        val resultGame = Intent()
                        resultGame.putExtra(EXTRA_GAME_NAME,gameName)
                        setResult(Activity.RESULT_OK , resultGame)
                        pbUploading.visibility = View.GONE
                        finish()
                    }
                    .show()
            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        // get the original bit map based on photo Uri
        val originalBitMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver , photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver , photoUri)
        }
        Log.i(TAG , "original bit map ${originalBitMap.width} the height : ${originalBitMap.height}")
        val scaledBitmap = BitMapScaler.scaleToFitHeight(originalBitMap , 250)
        Log.i(TAG , "Scaled Width ${scaledBitmap.width} the height : ${scaledBitmap.height}")
        val byteOutPutStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG ,60 , byteOutPutStream)
        return byteOutPutStream.toByteArray()
    }
}

