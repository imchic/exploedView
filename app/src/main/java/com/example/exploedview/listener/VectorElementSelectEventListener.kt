package com.example.exploedview.listener

import com.carto.core.MapPos
import com.carto.layers.EditableVectorLayer
import com.carto.layers.VectorElementEventListener
import com.carto.ui.VectorElementClickInfo
import com.carto.vectorelements.Text
import com.example.exploedview.MainActivity

class VectorElementSelectEventListener(private val activity: MainActivity, val layer: EditableVectorLayer?) :
    VectorElementEventListener() {

    private val _selectElementArr = mutableListOf<MapPos>()

    override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {

        activity.apply {
            if(_selectFlag || _groupFlag){
                select(clickInfo)
            }
        }

        return true
    }

    private fun select(element: VectorElementClickInfo) {

        layer?.selectedVectorElement = element.vectorElement

        val centerPos = element.vectorElement.geometry.centerPos

        if(activity._selectFlag){
            when (element.vectorElement) {
                is Text -> {
                    activity.utils.logD("vectorElement Type => Text")
                }
                else -> {
                    activity.utils.logD("vectorElement Type => EditableLayer")

                    _selectElementArr.apply {
                        // 신규
                        if (isEmpty()) {
                            add(centerPos)
                            activity.togglePolygonStyle("select", centerPos)

                            // 중복
                        } else {
                            if (!contains(centerPos)) {
                                add(centerPos)
                                activity.togglePolygonStyle("select", centerPos)

                            } else {
                                remove(centerPos)
                                activity.togglePolygonStyle("deselect", centerPos)

                            }
                        }
                    }

                }
            }
            getSelectElementArr()
        }

    }


    private fun getSelectElementArr(): List<MapPos> {
        activity.utils.logD("최종 배열 => $_selectElementArr [${_selectElementArr.size}]")
        return _selectElementArr
    }

}