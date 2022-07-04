package com.example.exploedview.listener

import android.util.Log
import com.carto.core.MapBounds
import com.carto.geometry.Geometry
import com.carto.geometry.LineGeometry
import com.carto.geometry.PointGeometry
import com.carto.geometry.PolygonGeometry
import com.carto.graphics.Color
import com.carto.layers.VectorEditEventListener
import com.carto.layers.VectorElementDragPointStyle
import com.carto.layers.VectorElementDragResult
import com.carto.styles.PointStyle
import com.carto.styles.PointStyleBuilder
import com.carto.ui.VectorElementDragInfo
import com.carto.vectorelements.Line
import com.carto.vectorelements.Point
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.VectorElement
import com.example.exploedview.MainActivity

class EditEventListener(private val activity: MainActivity) : VectorEditEventListener() {

    private var styleNormal: PointStyle? = null
    private var styleVirtual: PointStyle? = null
    private var styleSelected: PointStyle? = null

    private var _modifyElementBoudns: MapBounds? = null
    private var _withinPolygonArr: MutableList<Polygon>? = null

    override fun onElementModify(element: VectorElement?, geometry: Geometry?) {

        _modifyElementBoudns = MapBounds()
        _withinPolygonArr = mutableListOf()

        when (element) {
            is Point -> {
                element.geometry = geometry as PointGeometry
            }
            is Line -> {
                element.geometry = geometry as LineGeometry
            }
            is Polygon -> {
                element.geometry = geometry as PolygonGeometry

                for (i in 0 until element.geometry.poses.size()) {
                    Log.d("imchic", element.geometry.poses.get(i.toInt()).toString())
                }

            }
        }

        _modifyElementBoudns = element?.bounds
//        Log.d("imchic", _modifyElementBoudns.toString())

        activity.makePolygonArr.forEach {poly ->
           val withinPoly = _modifyElementBoudns?.contains(poly.bounds)
//            Log.d("imchic", withinPoly.toString())

            if(withinPoly == true){
                _withinPolygonArr?.add(poly)
            }

        }

        // 그룹영역에 포함된 Polygon 배열
        Log.d("imchic", "포함된 Polygon => ${_withinPolygonArr.toString()}")


    }

    override fun onElementDelete(element: VectorElement?) {

    }

    override fun onDragStart(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
        return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

    }

    override fun onDragMove(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
        return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY
    }

    override fun onDragEnd(dragInfo: VectorElementDragInfo?): VectorElementDragResult {
        return VectorElementDragResult.VECTOR_ELEMENT_DRAG_RESULT_MODIFY

    }

    override fun onSelectDragPointStyle(
        element: VectorElement?,
        dragPointStyle: VectorElementDragPointStyle
    ): PointStyle? {
        if (null == styleNormal) {
            val builder = PointStyleBuilder()
            builder.color = Color(255, 0, 0 , 255)
            builder.size = 15f
            styleNormal = builder.buildStyle()
            builder.size = 15f
            styleVirtual = builder.buildStyle()
            builder.color = Color(255, 0, 0 , 255)
            builder.size = 15f
            styleSelected = builder.buildStyle()
        }

        if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_NORMAL) {
            return styleSelected
        }
        return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) styleVirtual else styleNormal
    }
}


