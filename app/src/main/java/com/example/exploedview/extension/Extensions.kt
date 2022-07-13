package com.example.exploedview.extension

object Extensions {

    fun ArrayList<Int>.max(arr: ArrayList<Int>): Int {
        return arr.maxOrNull() ?: 0
    }

    fun ArrayList<Int>.min(arr: ArrayList<Int>): Int {
        return arr.minOrNull() ?: 0
    }
}