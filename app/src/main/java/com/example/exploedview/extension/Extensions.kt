package com.example.exploedview.extension

import java.util.Collections

object Extensions {

    fun ArrayList<Double>.max(arr: ArrayList<Double>): Double = Collections.max(arr).toDouble()

    fun ArrayList<Double>.min(arr: ArrayList<Double>): Double = Collections.min(arr)
}