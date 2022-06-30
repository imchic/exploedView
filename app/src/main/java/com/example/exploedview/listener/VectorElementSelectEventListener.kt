package com.example.exploedview.listener

import android.util.Log
import com.carto.core.MapPos
import com.carto.layers.VectorElementEventListener
import com.carto.ui.VectorElementClickInfo
import com.carto.vectorelements.Text
import com.example.exploedview.MainActivity

class VectorElementSelectEventListener(val mainActivity: MainActivity) :
    VectorElementEventListener() {

    private val _selectElementArr = mutableListOf<MapPos>()

    override fun onVectorElementClicked(clickInfo: VectorElementClickInfo): Boolean {
        select(clickInfo)
        return true
    }

    private fun select(element: VectorElementClickInfo) {

        val centerPos = element.vectorElement.geometry.centerPos

        when (element.vectorElement) {
            is Text -> {
                Log.d("imchic", "vectorElement Type => Text")
            }
            else -> {
                Log.d("imchic", "vectorElement Type => EditableLayer")

                _selectElementArr.apply {
                    // 신규
                    if (isEmpty()) {
                        add(centerPos)
                        mainActivity.togglePolygonStyle("select", centerPos)

                    // 중복
                    } else {
                        if (!contains(centerPos)) {
                            add(centerPos)
                            mainActivity.togglePolygonStyle("select", centerPos)

                        } else {
                            remove(centerPos)
                            mainActivity.togglePolygonStyle("deselect", centerPos)

                        }
                    }
                }

            }
        }

        getSelectElementArr()
    }


    private fun getSelectElementArr(): List<MapPos> {
        Log.d("imchic", "최종 배열 => $_selectElementArr [${_selectElementArr.size}]")
        return _selectElementArr
    }

}