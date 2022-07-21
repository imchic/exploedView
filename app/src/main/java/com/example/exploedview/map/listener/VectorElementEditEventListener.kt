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
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.util.LogUtil

class VectorElementEditEventListener(val source: LocalVectorDataSource?) : VectorEditEventListener() {

    private var styleNormal: PointStyle? = null
    private var styleVirtual: PointStyle? = null
    private var styleSelected: PointStyle? = null

    private var modifyElementBounds: MapBounds? = null

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
                val withinPoly = modifyElementBounds?.contains(it.bounds)
                if (withinPoly == true)
                    BaseMap.containsPolygonArr.add(it)
            }

            for (i in 0 until source?.all?.size()!!) {
                when (source.all?.get(i.toInt())) {
                    is BalloonPopup -> {
                        (source.all?.get(i.toInt()) as BalloonPopup).apply {
                            title = "드래그를 이용하여 영역을 지정해주세요."
                            description = "포함된 폴리곤의 개수 : ${BaseMap.containsPolygonArr.size}"
                        }.also {
                            BaseMap.mapViewModel.getGroupExplodedPolygon.value = BaseMap.containsPolygonArr.size.toString()
                        }
                    }
//                    is Polygon -> {
//                        (source.all?.get(i.toInt()) as Polygon).apply {
//                            style = MapStyle.setPolygonStyle(
//                                MapElementColor.set(ColorEnum.MAGENTA),
//                                MapElementColor.set(ColorEnum.MAGENTA),
//                                2F
//                            )
//                        }
//                    }
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
        dragPointStyle: VectorElementDragPointStyle
    ): PointStyle? {
        if (null == styleNormal) {
            val builder = PointStyleBuilder()
            builder.color = MapElementColor.set(ColorEnum.RED)
            builder.size = 15f
            styleNormal = builder.buildStyle()
            builder.size = 15f
            styleVirtual = builder.buildStyle()
            builder.color = MapElementColor.set(ColorEnum.RED)
            builder.size = 15f
            styleSelected = builder.buildStyle()
        }

        if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_NORMAL) {
            return styleSelected
        }
        return if (dragPointStyle == VectorElementDragPointStyle.VECTOR_ELEMENT_DRAG_POINT_STYLE_VIRTUAL) styleVirtual else styleNormal
    }
}


