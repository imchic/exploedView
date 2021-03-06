package com.example.exploedview.map.listener

import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.datasources.LocalVectorDataSource
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
import com.carto.styles.LineJoinType
import com.carto.ui.ClickType
import com.carto.ui.MapClickInfo
import com.carto.ui.MapEventListener
import com.carto.ui.MapView
import com.carto.vectorelements.*
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.map.MapLayerName
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil

class MapCustomEventListener(
    _mapView: MapView, private var _source: LocalVectorDataSource?, private val _posArr: MutableList<MapPos>?
) : MapEventListener() {

    private var _popup: BalloonPopup? = null
    private var _pointSymbol: Point? = null
    private var _lineSymbol: Line? = null
    private var _polygonSymbol: Polygon? = null

    private var _targetLayerNm: String = ""
    private var _targetLayerArr: MutableList<Layer>? = null

    init {

        _targetLayerArr = mutableListOf()

        for (i in 0 until BaseMap.getLayerCount()) {

            _targetLayerNm = BaseMap.getLayerName(i, "name")

            _targetLayerNm.run {
                when (this) {
                    MapLayerName.GROUP.value, MapLayerName.EXPLODED_VIEW.value, MapLayerName.ADD_FLOOR.value, MapLayerName.ADD_LINE.value -> {
                        _targetLayerArr?.add(_mapView.layers.get(i))
                    }
                    else -> {
                        return@run
                    }
                }
            }

        }

        _targetLayerArr?.map {
            BaseMap.selectListener = VectorElementSelectEventListener(it as EditableVectorLayer)

            it.run {
                if (it.getMetaDataElement("name").string == MapLayerName.GROUP.value) {
                    vectorEditEventListener = VectorElementEditEventListener(BaseMap.groupLayerSource)
                }
                vectorElementEventListener = BaseMap.selectListener

            }
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
                else -> throw Exception("???????????? ?????? ????????? ??????")
            }

            if (_popup != null) {
                _source?.remove(_popup)
                _popup = null
            }

            mapClickInfo.clickPos
            val popupStyle = MapStyle.setBallonPopupStyle(10)
            val clickPosCnt: Int? = _posArr?.size
            val clickPosCntTxt = "???????????? ?????? : $clickPosCnt"

            // ?????????
            when (clickPosCnt) {

                1 -> {
                    for (pos in _posArr!!) {
                        _pointSymbol = Point(pos, MapStyle.setPointStyle(MapElementColor.set(ColorEnum.MAGENTA), 13F))
                        element.add(_pointSymbol)

                        _popup = BalloonPopup(_pointSymbol?.geometry?.centerPos, popupStyle, "??? 3??? ???????????? ???????????????.",
                            clickPosCntTxt
                        )
                        element.add(_popup)
                    }
                }

                // ??????
                2 -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _lineSymbol = Line(
                        posVector, MapStyle.setLineStyle(
                            MapElementColor.set(ColorEnum.MAGENTA), LineJoinType.LINE_JOIN_TYPE_MITER, 8F
                        )
                    )
                    element.add(_lineSymbol)

                    _popup = BalloonPopup(_lineSymbol?.geometry?.centerPos, popupStyle, "??? 3??????????????? ???????????????.", clickPosCntTxt)
                    element.add(_popup)
                }

                // ?????????
                else -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _polygonSymbol = Polygon(
                        posVector, MapStyle.setPolygonStyle(
                            MapElementColor.set(ColorEnum.MAGENTA), MapElementColor.set(ColorEnum.MAGENTA), 2F
                        )
                    )

                    i("click create polygon => $posVector")
                    element.add(_polygonSymbol)

                    _popup = BalloonPopup(_polygonSymbol?.geometry?.centerPos, popupStyle, "???????????? ???????????? ????????? ??????????????????.", clickPosCntTxt)
                    element.add(_popup)
                }

            }

            i("element size => ${element.size()}")
            _source?.clear()
            _source?.addAll(element)

        }

    }
}