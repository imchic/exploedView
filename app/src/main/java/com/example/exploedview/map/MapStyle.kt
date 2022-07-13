package com.example.exploedview.map

import com.carto.core.Variant
import com.carto.datasources.GeoJSONVectorTileDataSource
import com.carto.graphics.Color
import com.carto.layers.VectorTileLayer
import com.carto.styles.*
import com.carto.ui.MapView
import com.carto.utils.AssetUtils
import com.carto.utils.ZippedAssetPackage
import com.carto.vectortiles.MBVectorTileDecoder
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.util.LogUtil
import java.io.InputStream


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
        _pointStyleBuilder.apply {
            this.color = color
            this.size = size
            return this.buildStyle()
        }

    }

    /**
     * 벡터 라인 스타일
     * @param color Color
     * @param type LineJoinType
     * @param width Float
     * @return LineStyle?
     */
    fun setLineStyle(color: Color, type: LineJoinType, width: Float): LineStyle? {
        _lineStyleBuilder.apply {
            this.color = color
            lineJoinType = type
            this.width = width
            return buildStyle()
        }

    }

    /**
     * 벡터 텍스트 스타일
     * @param color Color
     * @param fontSize Float
     * @return TextStyle?
     */
    fun setTextStyle(color: Color, fontSize: Float): TextStyle? {
        val _color = Color(color.r, color.g, color.b, 255)
        _textStyleBuilder.run {
            this.color = _color
            strokeColor = _color
            strokeWidth = 0.1F
            this.fontSize = fontSize
            this.textMargins = TextMargins(6, 6, 6, 6)
            return this.buildStyle()
        }

    }

    /**
     * 벡터 폴리곤 스타일
     * @param polygonColor Color
     * @param lineColor Color
     * @param lineWidth Float
     * @return PolygonStyle?
     */
    fun setPolygonStyle(polygonColor: Color, lineColor: Color, lineWidth: Float): PolygonStyle? {
        val _color = Color(polygonColor.r, polygonColor.g, polygonColor.b, 75)
        _polygonStyleBuilder.color = _color
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

        _ballonPopupStyleBuilder.apply {
//            cornerRadius = radius
            leftColor = Color(MapConst.STROKE_OPACITY, 0, 0, 0)
            leftMargins = BalloonPopupMargins(6, 6, 6, 6)
            rightMargins = BalloonPopupMargins(2, 6, 12, 6)
            leftColor = MapElementColor.set(ColorEnum.TEAL)
            titleColor = MapElementColor.set(ColorEnum.NAVY)
            titleFontSize = 16
            descriptionColor = MapElementColor.set(ColorEnum.GRAY)
            descriptionFontSize = 14
            placementPriority = 1
        }

        return _ballonPopupStyleBuilder.buildStyle()
    }

    fun createGeoJSONLayer(activity: MapActivity, mapView: MapView) {

        val dataSource = GeoJSONVectorTileDataSource(0, mapView.zoom.toInt())

        try {
            val `is`: InputStream = activity.assets.open("test.geojson")
            val sb = StringBuilder()
            var ch: Int
            while (`is`.read().also { ch = it } != -1) {
                sb.append(ch.toChar())
            }
            val data: Variant = Variant.fromString(sb.toString())
            val layerIdx = dataSource.createLayer("items")
            dataSource.setLayerGeoJSON(layerIdx, data)
            val styleAsset = ZippedAssetPackage(AssetUtils.loadAsset("test.zip"))
            val styleSet = CompiledStyleSet(styleAsset, "voyager")
            val decoder = MBVectorTileDecoder(styleSet)
            val layer = VectorTileLayer(dataSource, decoder)
            mapView.layers.add(layer)
        } catch (e: Exception) {
            LogUtil.e(e.toString())
        }

    }

}