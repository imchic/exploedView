package com.example.exploedview.map.listener

import com.carto.core.MapPos
import com.carto.layers.EditableVectorLayer
import com.carto.layers.VectorElementEventListener
import com.carto.ui.VectorElementClickInfo
import com.carto.vectorelements.Text
import com.example.exploedview.MainActivity

class VectorElementSelectEventListener(private val activity: MainActivity, val layer: EditableVectorLayer?) :
    VectorElementEventListener() {
    override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
        select(clickInfo)
        return true
    }

    private fun select(element: VectorElementClickInfo) {
        layer?.apply { selectedVectorElement = element.vectorElement }
    }

}