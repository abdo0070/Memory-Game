package com.abdalla_mmdouh.myapplication.models

import android.graphics.Bitmap

object BitMapScaler {
    fun scaleToFitHeight(b : Bitmap ,height : Int) : Bitmap{
        val factor = height / b.height.toFloat()
        return Bitmap.createScaledBitmap(b,(b.width * factor).toInt() , height , true)
    }
    fun scaleToFitWidth(b : Bitmap , width : Int) : Bitmap{
        val factor = width / b.width.toFloat()
        return Bitmap.createScaledBitmap(b,width,(factor*b.height).toInt() , true)
    }
}
