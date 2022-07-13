package com.example.exploedview.map

import com.carto.graphics.Color
import com.example.exploedview.util.LogUtil
import com.example.exploedview.base.BaseException
import com.example.exploedview.enums.ColorEnum

/**
 * Map Element Color Set
 */
object MapElementColor {

    var resultColor = Color()

    /**
     * 색상 지정
     * @param enum ColorEnum
     * @param a Short
     * @return Color
     */
    fun set(enum: ColorEnum, a: Short): Color {
        try {
            resultColor = when (enum) {
                ColorEnum.RED -> Color(255, 0, 0, a)
                ColorEnum.YELLOW -> Color(255, 255, 0, a)
                ColorEnum.ORANGE -> Color(255, 128, 0, a)
                ColorEnum.GREEN -> Color(0, 204, 102, a)
                ColorEnum.PINK -> Color(255, 0, 243, a)
                ColorEnum.PURPLE -> Color(135, 0, 255, a)
                else -> throw BaseException("등록된 컬러가 아닙니다.")
            }
        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }
        return resultColor
    }
}