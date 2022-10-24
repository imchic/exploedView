package com.example.exploedview.map.listener

import com.carto.core.MapBounds
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.Geometry
import com.carto.geometry.LineGeometry
import com.carto.geometry.PointGeometry
import com.carto.geometry.PolygonGeometry
import com.carto.layers.VectorEditEventListener
import com.carto.layers.VectorElementDragPointStyle
import com.carto.layers.VectorElementDragResult
import com.carto.styles.PointStyle
import com.carto.styles.PointStyleBuilder
import com.carto.ui.VectorElementDragInfo
import com.carto.vectorelements.*
import com.example.exploedview.map.BaseMap
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor

class VectorElementEditEventListener(private val source: LocalVectorDataSource?) : VectorEditEventListener() {

    private lateinit var styleNormal: PointStyle
    private lateinit var styleVirtual: PointStyle
    private lateinit var styleSelected: PointStyle

    private lateinit var modifyElementBounds: MapBounds

    override fun onElementModify(element: VectorElement?, geometry: Geometry?) {

        LogUtil.run {
            modifyElementBounds = MapBounds()

            when (element) {
                is Point -> {
                    element.geometry = geometry as PointGeometry
                }

                is Line -> {
                    element.geometry = geometry as LineGeometry
                }

                is Polygon -> {
                    element.geometry = geometry as PolygonGeometry

                    BaseMap.containsPolygonArr = mutableListOf()
                    modifyElementBounds = element.bounds

                }
            }

            BaseMap.createPolygonArr.forEach {
                val withinPoly = modifyElementBounds.contains(it.bounds)
                if (withinPoly)
                    BaseMap.containsPolygonArr.add(it)
            }

            for (i in 0 until source?.all?.size()!!) {
                when (source.all?.get(i.toInt())) {
                    is BalloonPopup -> {
                        (source.all?.get(i.toInt()) as BalloonPopup).apply {
                            title = "드래그를 이용하여 영역을 지정해주세요."
                            description = "포함된 폴리곤의 개수 : ${BaseMap.containsPolygonArr.size}"
                        }.also {
                            BaseMap.activity.vm.getGroupExplodedPolygon(BaseMap.containsPolygonArr.size)
                            BaseMap.selectPolygonArr.clear()
                        }
                    }
                }

            }


        }

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
        dragPointStyle: VectorElementDragPointStyle,
    ): PointStyle {

        val builder = PointStyleBuilder()
        builder.run {
            color = MapColor.RED
            size = 15f
            styleNormal = buildStyle()
            size = 15f
            styleVirtual = buildStyle()
            color = MapColor.RED
            size = 15f
            styleSelected = buildStyle()
        }

        if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_NORMAL) {
            return styleSelected
        }
        return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) styleVirtual else styleNormal
    }
}


