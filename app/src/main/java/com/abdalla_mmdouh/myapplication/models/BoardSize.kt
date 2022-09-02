package com.abdalla_mmdouh.myapplication.models

enum class BoardSize(val numCard : Int) {
    EASY(8), // 4 * 2
    MEDIUM(18),// 9 * 3
    HARD(24); // 6 * 4
    companion object {
        fun getByValue (value : Int) = values().first {it.numCard == value}
    }
    fun getWidth() : Int
    {
        return when(this)
        {
            EASY -> 2
            MEDIUM-> 3
            HARD -> 4
        }
    }
    fun getHeight() : Int
    {
        return when(this)
        {
            EASY -> 4
            MEDIUM-> 6
            HARD -> 6
        }
    }
    fun getPairs() : Int
    {
        return numCard / 2
    }
}