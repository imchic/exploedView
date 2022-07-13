package com.example.exploedview.map.listener

import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.layers.EditableVectorLayer
import com.carto.styles.LineJoinType
import com.carto.ui.ClickType
import com.carto.ui.MapClickInfo
import com.carto.ui.MapEventListener
import com.carto.ui.MapView
import com.carto.vectorelements.*
import com.example.exploedview.MainActivity
import com.example.exploedview.MapConst
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil

class MapCustomEventListener(
    private val activity: MainActivity,
    mapView: MapView?,
    var source: LocalVectorDataSource?,
    val layer: EditableVectorLayer?,
    val posArr: MutableList<MapPos>?
) : MapEventListener() {

    var popup: BalloonPopup? = null
    var point: Point? = null
    var line: Line? = null
    var polygon: Polygon? = null

    init {

        activity.selectListener = VectorElementSelectEventListener(activity, layer)

        layer?.run {
            vectorEditEventListener = VectorElementEditEventListener(activity)
            vectorElementEventListener = activity.selectListener
        }

        mapView?.layers?.add(layer)
        activity.setLayerName(layer, "layerName", Variant("groupLayer"))
    }

    override fun onMapClicked(mapClickInfo: MapClickInfo?) {
        super.onMapClicked(mapClickInfo)

        val element = VectorElementVector()
        val posVector = MapPosVector()

        LogUtil.run {
            when (mapClickInfo?.clickType) {
                ClickType.CLICK_TYPE_SINGLE -> {
                    i("single map click!")
                    posArr?.add(mapClickInfo.clickPos)
                }
                ClickType.CLICK_TYPE_LONG -> i("Long map click!")
                ClickType.CLICK_TYPE_DOUBLE -> i("Double map click!")
                ClickType.CLICK_TYPE_DUAL -> i("Dual map click!")
                else -> throw Exception("유효하지 않는 이벤트 발생")
            }

            if (popup != null) {
                source?.remove(popup)
                popup = null
            }

            val clickPos = mapClickInfo.clickPos
            val popupStyle = MapStyle.setBallonPopupStyle(10)

            val clickPosCnt = posArr?.size
            i("clickPosCnt => [$clickPosCnt]")

            // 포인트
            when (clickPosCnt) {

                1 -> {
                    for (pos in posArr!!) {
                        point = Point(pos, MapStyle.setPointStyle(MapElementColor.set(ColorEnum.PINK, MapConst.FILL_OPACITY), 13F))
                        element.add(point)

                        popup = BalloonPopup(clickPos, popupStyle, "point", clickPosCnt.toString())
                        element.add(popup)
                    }
                }

                // 라인
                2 -> {
                    for (pos in posArr!!) {
                        posVector.add(pos)
                    }
                    line = Line(
                        posVector,
                        MapStyle.setLineStyle(MapElementColor.set(ColorEnum.PINK, MapConst.FILL_OPACITY), LineJoinType.LINE_JOIN_TYPE_MITER, 8F)
                    )
                    element.add(line)

                    popup = BalloonPopup(clickPos, popupStyle, "line", clickPosCnt.toString())
                    element.add(popup)
                }

                // 폴리곤
                else -> {
                    for (pos in posArr!!) {
                        posVector.add(pos)
                    }
                    polygon = Polygon(
                        posVector,
                        MapStyle.setPolygonStyle(MapElementColor.set(ColorEnum.PINK, MapConst.FILL_OPACITY), MapElementColor.set(ColorEnum.PINK, 255), 2F)
                    )

                    i("click create polygon => $posVector")
                    element.add(polygon)

                    popup = BalloonPopup(clickPos, popupStyle, "polygon", clickPosCnt.toString())
                    element.add(popup)
                }

            }

            i("element size => ${element.size()}")
            source?.clear()
            source?.addAll(element)

        }

    }
}