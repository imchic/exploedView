package com.example.exploedview.map.listener

import com.carto.layers.EditableVectorLayer
import com.carto.layers.VectorElementEventListener
import com.carto.ui.VectorElementClickInfo

class VectorElementSelectEventListener(val layer: EditableVectorLayer?) :
    VectorElementEventListener() {
    override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
        select(clickInfo)
        return true
    }

    private fun select(element: VectorElementClickInfo) {
        layer?.apply { selectedVectorElement = element.vectorElement }
    }

}