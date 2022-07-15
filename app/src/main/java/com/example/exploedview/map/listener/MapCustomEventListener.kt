package com.example.exploedview.map.listener

import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.datasources.LocalVectorDataSource
import com.carto.layers.EditableVectorLayer
import com.carto.styles.LineJoinType
import com.carto.ui.ClickType
import com.carto.ui.MapClickInfo
import com.carto.ui.MapEventListener
import com.carto.ui.MapView
import com.carto.vectorelements.*
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil

class MapCustomEventListener(
    _mapView: MapView,
    private var _source: LocalVectorDataSource?,
    private val _posArr: MutableList<MapPos>?
) : MapEventListener() {

    private var _popup: BalloonPopup? = null
    private var _pointSymbol: Point? = null
    private var _lineSymbol: Line? = null
    private var _polygonSymbol: Polygon? = null

    private var _targetLayer: EditableVectorLayer? = null

    init {

        for(i in 0 until BaseMap.getLayerCount()){

            val targetLayerNm = BaseMap.getLayerName(i, "name")
            if(targetLayerNm == "group"){
                _targetLayer = _mapView.layers.get(i) as EditableVectorLayer?
                break
            }

        }

        BaseMap.selectListener = VectorElementSelectEventListener(_targetLayer)

        _targetLayer?.run {
            vectorEditEventListener = VectorElementEditEventListener()
            vectorElementEventListener = BaseMap.selectListener
        }
    }

//    override fun onMapMoved() {
//        super.onMapMoved()
//        LogUtil.i(mapView?.zoom.toString())
//        LogUtil.i(mapView?.focusPos.toString())
//    }

    override fun onMapClicked(mapClickInfo: MapClickInfo?) {
        super.onMapClicked(mapClickInfo)

        val element = VectorElementVector()
        val posVector = MapPosVector()

        LogUtil.run {
            when (mapClickInfo?.clickType) {
                ClickType.CLICK_TYPE_SINGLE -> {
//                    i("single map click!")
                    _posArr?.add(mapClickInfo.clickPos)
                }
                ClickType.CLICK_TYPE_LONG -> i("Long map click!")
                ClickType.CLICK_TYPE_DOUBLE -> i("Double map click!")
                ClickType.CLICK_TYPE_DUAL -> i("Dual map click!")
                else -> throw Exception("유효하지 않는 이벤트 발생")
            }

            if (_popup != null) {
                _source?.remove(_popup)
                _popup = null
            }

            val clickPos = mapClickInfo.clickPos
            val popupStyle = MapStyle.setBallonPopupStyle(10)

            // 포인트
            when (val clickPosCnt = _posArr?.size) {

                1 -> {
                    for (pos in _posArr!!) {
                        _pointSymbol = Point(pos, MapStyle.setPointStyle(MapElementColor.set(ColorEnum.BLUE), 13F))
                        element.add(_pointSymbol)

                        _popup = BalloonPopup(clickPos, popupStyle, "point", clickPosCnt.toString())
                        element.add(_popup)
                    }
                }

                // 라인
                2 -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _lineSymbol = Line(
                        posVector,
                        MapStyle.setLineStyle(
                            MapElementColor.set(ColorEnum.BLUE),
                            LineJoinType.LINE_JOIN_TYPE_MITER,
                            8F
                        )
                    )
                    element.add(_lineSymbol)

                    _popup = BalloonPopup(clickPos, popupStyle, "line", clickPosCnt.toString())
                    element.add(_popup)
                }

                // 폴리곤
                else -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _polygonSymbol = Polygon(
                        posVector,
                        MapStyle.setPolygonStyle(
                            MapElementColor.set(ColorEnum.BLUE),
                            MapElementColor.set(ColorEnum.BLUE),
                            2F
                        )
                    )

                    i("click create polygon => $posVector")
                    element.add(_polygonSymbol)

                    _popup = BalloonPopup(clickPos, popupStyle, "polygon", clickPosCnt.toString())
                    element.add(_popup)
                }

            }

            i("element size => ${element.size()}")
            _source?.clear()
            _source?.addAll(element)

        }

    }
}