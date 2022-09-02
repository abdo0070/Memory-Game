package com.abdalla_mmdouh.myapplication

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.abdalla_mmdouh.myapplication.models.BoardSize
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.image_card.view.*


class CustomMemoryGameAdapter
    (private val context : Context  ,
     private  val imagesUri : List<Uri> ,
     private val boardSize : BoardSize,
    private val ImageClickListner : ImageClickListener)
    :RecyclerView.Adapter<CustomMemoryGameAdapter.ViewHolder>() {
    interface ImageClickListener{
        fun selectPhoto()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_card, parent , false)
        // change the height and weidth of the image view
        val carWidth = parent.width / boardSize.getWidth() -(5 *2)
        val carHeight = parent.height / boardSize.getHeight() -(5*2)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImages).layoutParams
        layoutParams.width = carWidth
        layoutParams.height = carHeight
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imagesUri.size)
        {
            holder.bind(imagesUri[position])
        }else{
            holder.bind()
        }

    }

    override fun getItemCount(): Int = boardSize.getPairs()

    inner class ViewHolder (item : View): RecyclerView.ViewHolder(item)
    {
       private var customImage = itemView.findViewById<ImageView>(R.id.ivCustomImages)
        fun bind (uri: Uri)
        {
            customImage.setImageURI(uri)
            customImage.setOnClickListener(null)

        }
        fun bind(){
            // make user select photo
            customImage.setOnClickListener{
                ImageClickListner.selectPhoto()
            }
        }

    }
}
