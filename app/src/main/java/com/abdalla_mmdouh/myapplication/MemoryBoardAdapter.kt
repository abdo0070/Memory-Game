package com.abdalla_mmdouh.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdalla_mmdouh.myapplication.models.BoardSize
import com.abdalla_mmdouh.myapplication.models.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min
import kotlinx.android.synthetic.main.box.view.*

class MemoryBoardAdapter
    (private val context : Context,
     private val boardSize : BoardSize ,
     private val cards : List<MemoryCard>,
     private val CardClickedListener : CardClickListener):
     RecyclerView.Adapter<MemoryBoardAdapter.ItemViewHolder>() {
    interface CardClickListener
    {
        fun onCardClicked(position: Int)
    }
    companion object
    {
        private const val MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdapter"
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.box, parent , false)

       val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val carSideLength = min(cardHeight,cardWidth)

        val layoutParams = view.cvBox.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardWidth
        layoutParams.height = cardHeight
        layoutParams.setMargins(MARGIN_SIZE , MARGIN_SIZE , MARGIN_SIZE , MARGIN_SIZE)

        return ItemViewHolder(view)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.bind(position)
    }

    override fun getItemCount(): Int = boardSize.numCard
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        fun bind (position: Int)
        {

            val card = cards[position]
            if (card.isShowen){
                if (card.imageUrl != null){
                    Picasso.get().load(card.imageUrl).placeholder(R.drawable.ic_image).into(itemView.imageButton)
                }else{
                    itemView.imageButton.setImageResource(card.identifier) }
            }
            else{
                itemView.imageButton.setImageResource(R.drawable.ic_launcher_background)
            }
            itemView.imageButton.alpha = if (card.isPair) .4f else 1.0f

            itemView.imageButton.setOnClickListener {
                Log.i(TAG , "Clicked on ${position+1}")
                CardClickedListener.onCardClicked(position)
            }
        }
    }
}