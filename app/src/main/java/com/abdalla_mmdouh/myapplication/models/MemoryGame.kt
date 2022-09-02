package com.abdalla_mmdouh.myapplication.models

import android.content.Context
import android.view.View
import androidx.core.content.contentValuesOf
import com.abdalla_mmdouh.myapplication.DEFUALT
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.text.FieldPosition

class MemoryGame
    (private val boardSize: BoardSize,
     customGameImages: List<String>?){
    var cardIndex : Int ? = null

    val cards : List<MemoryCard>
    var numOfPairs : Int = 0
    var numOfMoves : Int = 0
    init {
        if (customGameImages == null){
        var images = DEFUALT.shuffled().take(boardSize.getPairs())
        images = (images + images).shuffled()
        cards = images.map { MemoryCard(it) }
        }else{
            var randomizeImages = (customGameImages + customGameImages).shuffled()
            cards = randomizeImages.map { MemoryCard(it.hashCode(),it) }
        }

    }
    fun isFLipped(index : Int) : Boolean
    {
        var foundMatch = false
        // 0 Card Selected show one
        // 1 Card Selected check and show two
        // 2 Select and one and don not show two
        if (cardIndex == null)
        {
            addCards(index)
            cardIndex = index
        }
        else
        {
          foundMatch =  checkMatch(index, cardIndex!!)
            cardIndex = null
        }
        if (!cards[index].isPair) {
            cards[index].isShowen = !cards[index].isShowen
            numOfMoves++
        }
        else cards[index].isShowen = true
        return foundMatch
    }
    fun addCards(index: Int)
    {
        for (card in cards)
        {
            if (!card.isPair)
            {
                card.isShowen = false
            }
        }

    }
    fun checkMatch(position1: Int,position2: Int ) : Boolean
    {
        if (cards[position1].identifier != cards[position2].identifier ||cards[position1] == cards[position2] )
        {
            return false
        }
            cards[position1].isPair = true
            cards[position2].isPair = true
        if (!winGame())
            numOfPairs++
            return true
    }
    fun winGame() : Boolean
    {
        return boardSize.getPairs() == numOfPairs
    }

    // Add Snack bar You Win
    // Add Argb
}