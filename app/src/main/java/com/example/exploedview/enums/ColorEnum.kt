package com.example.exploedview.enums

import com.carto.graphics.Color

const val alpha: Short = 255
enum class ColorEnum(val value: Color) {

    BLACK(Color(0, 0, 0, alpha)),
    WHITE(Color(255, 255, 255, alpha)),
    RED(Color(255, 0, 0, alpha)),
    LIME(Color(0, 255, 0, alpha)),
    BLUE(Color(0, 0, 255, alpha)),
    YELLOW(Color(255, 255, 0, alpha)),
    CYAN(Color(0,255, 255, alpha)),
    MAGENTA(Color(255,0, 255, alpha)),
    SILVER(Color(192,192, 192, alpha)),
    GRAY(Color(128,128, 128, alpha)),
    MAROON(Color(128,0, 0, alpha)),
    OLIVE(Color(128,128, 0, alpha)),
    ORANGE(Color(255, 165, 0, alpha)),
    GREEN(Color(0, 128, 0, alpha)),
    PINK(Color(255, 192, 203, alpha)),
    TEAL(Color(0, 128, 128, alpha)),
    NAVY(Color(0, 0, 128, alpha)),
    HOTPINK(Color(255, 105, 180, alpha)),
    PURPLE(Color(128, 0, 128, alpha)),
    SKYBLUE(Color(135, 206, 235, alpha)),
    BROWN(Color(165, 42, 42, alpha)),
}