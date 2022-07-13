package com.example.exploedview.map.listener

import com.carto.layers.EditableVectorLayer
import com.carto.ui.MapClickInfo
import com.carto.ui.MapEventListener

class VectorElementDeselectListener(private val layer: EditableVectorLayer?) : MapEventListener() {
    override fun onMapClicked(mapClickInfo: MapClickInfo?) {
        deselect(layer)
    }

    private fun deselect(layer: EditableVectorLayer?) {
        layer?.selectedVectorElement = null
    }
}