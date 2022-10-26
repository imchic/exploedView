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
import com.example.exploedview.base.BaseException
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayerName
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor

class MapCustomEventListener(
    private val _mapView: MapView, private var _source: LocalVectorDataSource?, private val _posArr: MutableList<MapPos>?
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
                    MapLayerName.GROUP.value,
                    MapLayerName.EXPLODED_VIEW.value,
                    MapLayerName.ADD_FLOOR.value,
                    MapLayerName.ADD_LINE.value -> {
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
                    vectorEditEventListener = VectorElementEditEventListener(BaseMap.containsDataSource)
                }
                vectorElementEventListener = BaseMap.selectListener

            }
        }

    }

    override fun onMapMoved() {
        super.onMapMoved()
        try {
            BaseMap.activity.runOnUiThread {
                BaseMap.activity.vm.getCoordinates("${_mapView.focusPos.x} ${_mapView.focusPos.y}")
            }
        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }
//        LogUtil.i(_mapView.focusPos.toString())
    }

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
//                ClickType.CLICK_TYPE_LONG -> i("Long map click!")
//                ClickType.CLICK_TYPE_DOUBLE -> i("Double map click!")
//                ClickType.CLICK_TYPE_DUAL -> i("Dual map click!")
                else -> BaseMap.activity.vm.showSnackbarString("유효하지 않는 이벤트 발생")
            }

            if (_popup != null) {
                _source?.remove(_popup)
                _popup = null
            }

            mapClickInfo?.clickPos
            val popupStyle = MapStyle.setBalloonPopupStyle(10)
            val clickPosCnt: Int? = _posArr?.size
            val clickPosCntTxt = "포인트의 개수 : $clickPosCnt"

            // 포인트
            when (clickPosCnt) {

                1 -> {
                    for (pos in _posArr!!) {
                        _pointSymbol = Point(pos, MapStyle.setPointStyle(MapColor.MAGENTA, 13F))
                        element.add(_pointSymbol)

                        _popup = BalloonPopup(_pointSymbol?.geometry?.centerPos, popupStyle, "선 3개 이상부터 가능합니다.",
                            clickPosCntTxt
                        )
                        element.add(_popup)
                    }
                }

                // 라인
                2 -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _lineSymbol = Line(
                        posVector, MapStyle.setLineStyle(
                            MapColor.MAGENTA, LineJoinType.LINE_JOIN_TYPE_MITER, 8F
                        )
                    )
                    element.add(_lineSymbol)

                    _popup = BalloonPopup(_lineSymbol?.geometry?.centerPos, popupStyle, "선 3개이상부터 가능합니다.", clickPosCntTxt)
                    element.add(_popup)
                }

                // 폴리곤
                else -> {
                    for (pos in _posArr!!) {
                        posVector.add(pos)
                    }
                    _polygonSymbol = Polygon(
                        posVector, MapStyle.setPolygonStyle(
                            MapColor.MAGENTA, MapColor.MAGENTA, 2F
                        )
                    )

//                    i("click create polygon => $posVector")
                    element.add(_polygonSymbol)

                    _popup = BalloonPopup(_polygonSymbol?.geometry?.centerPos, popupStyle, "꼭지점을 이용하여 영역을 지정해주세요.", clickPosCntTxt)
                    element.add(_popup)
                }

            }

//            i("element size => ${element.size()}")
            _source?.clear()
            _source?.addAll(element)

        }

    }
}