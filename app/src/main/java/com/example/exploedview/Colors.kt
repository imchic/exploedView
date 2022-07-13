package com.example.exploedview

import com.carto.graphics.Color

object Colors {

    fun setFillColor(color: String, alpha: Short): Color {
        return when (color) {
            "red" -> Color(255, 255, 0, alpha)
            "orange" -> Color(255, 128, 0, alpha)
            "green" -> Color(0, 204, 102, alpha)
            else -> Color(255, 255, 255, alpha)
        }
    }
}