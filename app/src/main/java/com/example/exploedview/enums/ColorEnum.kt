package com.example.exploedview.enums

enum class ColorEnum(val r: Int, val g: Int, val b: Int, val a: Int) {
    RED(255, 0, 0, 30),
    ORANGE(255, 165, 0, 30),
    YELLOW(255, 255, 0, 30),
    GREEN(0, 255, 0, 30),
    BLUE(0, 0, 255, 30),
    INDIGO(75, 0, 130, 30),
    VIOLET(238, 130, 238, 30);

//    fun rgb() = (r * 256 + g) * 256 + b
}
