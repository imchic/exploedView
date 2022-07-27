package com.example.exploedview.util

import com.carto.graphics.Color

sealed class MapColor {
    object BLACK: Color(0, 0, 0, 255)
    object WHITE: Color(255, 255, 255, 255)
    object RED: Color(255, 0, 0, 255)
    object LIME: Color(0, 255, 0, 255)
    object BLUE: Color(0, 0, 255, 255)
    object YELLOW: Color(255, 255, 0, 255)
    object CYAN: Color(0,255, 255, 255)
    object MAGENTA: Color(255,0, 255, 255)
    object SILVER: Color(192,192, 192, 255)
    object GRAY: Color(128,128, 128, 255)
    object MAROON: Color(128,0, 0, 255)
    object OLIVE: Color(128,128, 0, 255)
    object ORANGE: Color(255, 165, 0, 255)
    object GREEN: Color(0, 128, 0, 255)
    object PINK: Color(255, 192, 203, 255)
    object TEAL: Color(0, 128, 128, 255)
    object NAVY: Color(0, 0, 128, 255)
    object HOTPINK: Color(255, 105, 180, 255)
    object PURPLE: Color(128, 0, 128, 255)
    object SKYBLUE: Color(135, 206, 235, 255)
    object BROWN: Color(165, 42, 42, 255)
}