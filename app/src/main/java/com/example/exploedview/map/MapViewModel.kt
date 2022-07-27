package com.example.exploedview.map

import com.example.exploedview.base.BaseViewModel
import com.example.exploedview.util.SingleLiveEvent

open class MapViewModel : BaseViewModel(){

//    private val _getSelectExplodedPolygon = SingleLiveEvent<Any>()
//    val getSelectExplodedPolygon: SingleLiveEvent<Any> get() = _getSelectExplodedPolygon

    val getMapEvent: SingleLiveEvent<String> by lazy { SingleLiveEvent() }

    val getTotalExplodedPolygon: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getBaseLayers: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getSelectExplodedPolygon: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getGroupExplodedPolygon: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getLayerReadStatus: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }

    val getCoord: SingleLiveEvent<String> by lazy { SingleLiveEvent() }

    val getAddFloorCnt: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getAddLineCnt: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getAddHoCnt: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }

    val getContainsCnt: SingleLiveEvent<Int> by lazy { SingleLiveEvent() }



}