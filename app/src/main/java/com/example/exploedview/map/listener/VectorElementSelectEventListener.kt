package com.example.exploedview.map.listener

import com.carto.layers.EditableVectorLayer
import com.carto.layers.VectorElementEventListener
import com.carto.ui.VectorElementClickInfo
import com.carto.vectorelements.Text
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayerName
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil
import com.fasterxml.jackson.databind.ser.Serializers.Base

class VectorElementSelectEventListener(private val layer: EditableVectorLayer?) :
    VectorElementEventListener() {
    override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {

        val selectLayerName = clickInfo.layer.getMetaDataElement("name").string

        when (selectLayerName) {
            MapLayerName.EXPLODED_VIEW.value, MapLayerName.GROUP.value, MapLayerName.ADD_FLOOR.value, MapLayerName.ADD_LINE.value -> {
                select(clickInfo)
            }
        }

        LogUtil.d("선택된 레이어 : $selectLayerName , ${clickInfo.clickPos}")
        return true
    }

    private fun select(element: VectorElementClickInfo) {
        layer?.apply {
            selectedVectorElement = element.vectorElement

            val centerPos = element.vectorElement.geometry.centerPos
            LogUtil.i(centerPos.toString())

            when (element.vectorElement) {
                is Text -> {
                    LogUtil.i("vectorElement Type => Text")
                }
                else -> {
                    LogUtil.i("vectorElement Type => EditableLayer")
                    BaseMap.select(element.vectorElement.geometry)
                }
            }

        }
    }
}
