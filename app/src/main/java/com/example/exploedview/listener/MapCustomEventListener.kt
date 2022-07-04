package com.example.exploedview.listener

import com.carto.core.MapPos
import com.carto.ui.ClickType
import com.carto.ui.MapClickInfo
import com.carto.ui.MapEventListener
import com.example.exploedview.MainActivity

class MapCustomEventListener(private val activity: MainActivity) : MapEventListener() {

    private var _groupMapPosArr = mutableListOf<MapPos>()

    override fun onMapClicked(mapClickInfo: MapClickInfo?) {
        super.onMapClicked(mapClickInfo)

        when(mapClickInfo?.clickType){
            ClickType.CLICK_TYPE_SINGLE -> {
                _groupMapPosArr.add(mapClickInfo.clickPos)
            }
            else -> null
        }

        activity.drawGroupBoundaryLayer(_groupMapPosArr)
    }

}