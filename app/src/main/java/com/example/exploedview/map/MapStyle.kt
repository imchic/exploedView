package com.example.exploedview.map

import com.carto.graphics.Color
import com.carto.styles.*
import com.example.exploedview.util.MapColor


object MapStyle {

    // style
    private var pointStyleBuilder = PointStyleBuilder()
    private var lineStyleBuilder = LineStyleBuilder()
    private var polygonStyleBuilder = PolygonStyleBuilder()
    private var textStyleBuilder = TextStyleBuilder()
    private var balloonPopupStyleBuilder = BalloonPopupStyleBuilder()


    /**
     * 벡터 포인트 스타일
     * @param color Color
     * @param size Float
     * @return PointStyle?
     */
    fun setPointStyle(color: Color, size: Float): PointStyle? {
        pointStyleBuilder.run {
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
        lineStyleBuilder.run {
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
        val textColor = Color(color.r, color.g, color.b, 255)
        textStyleBuilder.run {
            this.color = textColor
            strokeColor = textColor
            strokeWidth = 0.1F
            this.fontSize = fontSize
            textMargins = TextMargins(6, 6, 6, 6)
//            isHideIfOverlapped = false
            isScaleWithDPI = true
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
        val polyColor = Color(polygonColor.r, polygonColor.g, polygonColor.b, 75)
        polygonStyleBuilder.color = polyColor
        lineStyleBuilder.color = lineColor
        lineStyleBuilder.width = lineWidth
        polygonStyleBuilder.lineStyle = lineStyleBuilder.buildStyle()
        return polygonStyleBuilder.buildStyle()
    }

    /**
     * 벡터 팝업 스타일
     * @param radius Int
     * @return BalloonPopupStyle?
     */
    fun setBalloonPopupStyle(radius: Int?): BalloonPopupStyle? {
        balloonPopupStyleBuilder.run {
            cornerRadius = radius ?: 0
            leftColor = Color(MapConst.STROKE_OPACITY, 0, 0, 0)
            leftMargins = BalloonPopupMargins(6, 6, 6, 6)
            rightMargins = BalloonPopupMargins(2, 6, 12, 6)
            leftColor = MapColor.TEAL
            titleColor = MapColor.NAVY
            titleFontSize = 16
            descriptionColor = MapColor.GRAY
            descriptionFontSize = 14
            placementPriority = 1
        }
        return balloonPopupStyleBuilder.buildStyle()
    }

    /*fun createGeoJSONLayer(activity: MapActivity, mapView: MapView) {

        val dataSource = GeoJSONVectorTileDataSource(0, mapView.zoom.toInt())

        try {
            val `is`: InputStream = activity.assets.open("dusan.geojson")
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

    }*/

}