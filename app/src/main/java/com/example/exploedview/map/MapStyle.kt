package com.example.exploedview.map

import com.carto.graphics.Color
import com.carto.styles.*
import com.carto.vectorelements.Polygon
import com.example.exploedview.MapConst
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.util.LogUtil

object MapStyle {

    // style
    private var _pointStyleBuilder = PointStyleBuilder()
    private var _lineStyleBuilder = LineStyleBuilder()
    private var _polygonStyleBuilder = PolygonStyleBuilder()
    private var _textStyleBuilder = TextStyleBuilder()
    private var _ballonPopupStyleBuilder = BalloonPopupStyleBuilder()


    /**
     * 벡터 포인트 스타일
     * @param color Color
     * @param size Float
     * @return PointStyle?
     */
    fun setPointStyle(color: Color, size: Float): PointStyle? {
        _pointStyleBuilder.color = color
        _pointStyleBuilder.size = size
        return _pointStyleBuilder.buildStyle()
    }

    /**
     * 벡터 라인 스타일
     * @param color Color
     * @param type LineJoinType
     * @param width Float
     * @return LineStyle?
     */
    fun setLineStyle(color: Color, type: LineJoinType, width: Float): LineStyle? {
        _lineStyleBuilder.color = color
        _lineStyleBuilder.lineJoinType = type
        _lineStyleBuilder.width = width
        return _lineStyleBuilder.buildStyle()
    }

    /**
     * 벡터 텍스트 스타일
     * @param color Color
     * @param fontSize Float
     * @return TextStyle?
     */
    fun setTextStyle(color: Color, fontSize: Float): TextStyle? {
        _textStyleBuilder.color = color
        _textStyleBuilder.fontSize = fontSize
        _textStyleBuilder.orientationMode = BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA_GROUND
        _textStyleBuilder.isScaleWithDPI = false
        return _textStyleBuilder.buildStyle()
    }

    /**
     * 벡터 폴리곤 스타일
     * @param polygonColor Color
     * @param lineColor Color
     * @param lineWidth Float
     * @return PolygonStyle?
     */
    fun setPolygonStyle(polygonColor: Color, lineColor: Color, lineWidth: Float): PolygonStyle? {
        _polygonStyleBuilder.color = polygonColor
        _lineStyleBuilder.color = lineColor
        _lineStyleBuilder.width = lineWidth
        _polygonStyleBuilder.lineStyle = _lineStyleBuilder.buildStyle()
        return _polygonStyleBuilder.buildStyle()
    }

    /**
     * 벡터 팝업 스타일
     * @param radius Int
     * @return BalloonPopupStyle?
     */
    fun setBallonPopupStyle(radius: Int): BalloonPopupStyle? {
        _ballonPopupStyleBuilder.cornerRadius = radius
        _ballonPopupStyleBuilder.leftColor = Color(MapConst.STROKE_OPACITY, 0, 0, 0)
//        _ballonPopupStyleBuilder?.leftMargins = BalloonPopupMargins(6, 6, 6, 6)
//        _ballonPopupStyleBuilder?.leftImage = BitmapUtils.createBitmapFromAndroidBitmap(infoImage)
//        _ballonPopupStyleBuilder?.rightImage = BitmapUtils.createBitmapFromAndroidBitmap(arrowImage)
//        _ballonPopupStyleBuilder?.rightMargins = BalloonPopupMargins(2, 6, 12, 6)
        _ballonPopupStyleBuilder.placementPriority = 1
        return _ballonPopupStyleBuilder.buildStyle()
    }

    /**
     * 폴리곤 선택, 비선택 모듈
     * @param type String
     * @param mapPos MapPos
     */
    fun togglePolygonStyle(parentsArr: MutableList<Polygon>, childArr: MutableList<Polygon>, type: String) {

        runCatching {
            parentsArr.filter { x ->
                childArr.contains(x)
            }
        }.onSuccess {
            it.map { polygon ->
                when (type) {

                    "default" -> {
                        polygon.style = setPolygonStyle(
                            MapElementColor.set(ColorEnum.GREEN),
                            MapElementColor.set(ColorEnum.GREEN),
                            2F
                        )
                    }

                    "group" -> {
                        polygon.style = setPolygonStyle(
                            MapElementColor.set(ColorEnum.PURPLE),
                            MapElementColor.set(ColorEnum.PURPLE),
                            2F
                        )
                    }

                }
            }
        }
        .onFailure {
            LogUtil.e(it.toString())
        }
    }

}