package com.example.exploedview.map

import com.carto.graphics.Color
import com.example.exploedview.enums.ColorEnum

/**
 * Map Element Color Set
 */
object MapElementColor {

    /**
     * 색상 지정
     * @param enum ColorEnum
     * @param a Short
     * @return Color
     */
    fun set(enum: ColorEnum): Color {
        return enum.value
    }
}