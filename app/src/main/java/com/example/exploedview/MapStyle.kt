package com.example.exploedview

import com.carto.core.MapPos
import com.carto.graphics.Color
import com.carto.styles.*

object MapStyle {

    // style
    var _pointStyleBuilder = PointStyleBuilder()
    var _lineStyleBuilder = LineStyleBuilder()
    var _polygonStyleBuilder = PolygonStyleBuilder()
    var _textStyleBuilder = TextStyleBuilder()
    var _ballonPopupStyleBuilder = BalloonPopupStyleBuilder()

    /**
     * 벡터 포인트 스타일
     * @param color Color
     * @param size Float
     * @return PointStyle?
     */
    fun setPointStyle(color: Color, size: Float): PointStyle? {
        _pointStyleBuilder?.color = color
        _pointStyleBuilder?.size = size
        return _pointStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 라인 스타일
     * @param color Color
     * @param type LineJoinType
     * @param width Float
     * @return LineStyle?
     */
    fun setLineStyle(color: Color, type: LineJoinType, width: Float): LineStyle? {
        _lineStyleBuilder?.color = color
        _lineStyleBuilder?.lineJoinType = type
        _lineStyleBuilder?.width = width
        return _lineStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 텍스트 스타일
     * @param color Color
     * @param fontSize Float
     * @return TextStyle?
     */
    fun setTextStyle(color: Color, fontSize: Float): TextStyle? {
        _textStyleBuilder?.color = color
        _textStyleBuilder?.fontSize = fontSize
        _textStyleBuilder?.orientationMode = BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA_GROUND
        _textStyleBuilder?.isScaleWithDPI = false
        return _textStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 폴리곤 스타일
     * @param polygonColor Color
     * @param lineColor Color
     * @param lineWidth Float
     * @return PolygonStyle?
     */
    fun setPolygonStyle(polygonColor: Color, lineColor: Color, lineWidth: Float): PolygonStyle? {
        _polygonStyleBuilder?.color = polygonColor
        _lineStyleBuilder?.color = lineColor
        _lineStyleBuilder?.width = lineWidth
        _polygonStyleBuilder?.lineStyle = _lineStyleBuilder?.buildStyle()
        return _polygonStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 팝업 스타일
     * @param radius Int
     * @return BalloonPopupStyle?
     */
    fun setBallonPopupStyle(radius: Int): BalloonPopupStyle?{
        _ballonPopupStyleBuilder?.cornerRadius = radius
        _ballonPopupStyleBuilder?.leftColor = Color(255, 0, 0, 0)
//        _ballonPopupStyleBuilder?.leftMargins = BalloonPopupMargins(6, 6, 6, 6)
//        _ballonPopupStyleBuilder?.leftImage = BitmapUtils.createBitmapFromAndroidBitmap(infoImage)
//        _ballonPopupStyleBuilder?.rightImage = BitmapUtils.createBitmapFromAndroidBitmap(arrowImage)
//        _ballonPopupStyleBuilder?.rightMargins = BalloonPopupMargins(2, 6, 12, 6)
        _ballonPopupStyleBuilder?.placementPriority = 1
        return _ballonPopupStyleBuilder?.buildStyle()
    }

    /**
     * 폴리곤 선택, 비선택 모듈
     * @param type String
     * @param mapPos MapPos
     */
    fun togglePolygonStyle(type: String, mapPos: MapPos) {

//        createPolygonArr.forEach { data ->
//            if (data.geometry.centerPos == mapPos) {
//
//                when (type) {
//                    "select" -> data.style = setPolygonStyle(Color(255, 123, 0, _alpha), Color(0, 0, 0, 255), 2F)
//                    "deselect" -> data.style = setPolygonStyle(Color(255, 255, 0, _alpha), Color(0, 0, 0, 255), 2F)
//                }
//            }
//        }
//
//        utils.logI("response => $mapPos")
    }
}