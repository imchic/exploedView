package com.example.exploedview.map

import android.content.Context
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.MultiPolygonGeometry
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.carto.vectorelements.VectorElementVector
import com.example.exploedview.base.BaseException
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.extension.Extensions.max
import com.example.exploedview.util.LogUtil

object MapLayer {

    private var maxArr = arrayListOf<Int>()
    private var maxVal: Int? = null
    private var filterArr: MutableList<Polygon>? = null

    private lateinit var south: MapPos
    private lateinit var west: MapPos
    private lateinit var north: MapPos
    private lateinit var east: MapPos

    private lateinit var addPolygon: Polygon
    private lateinit var addText: Text
    private lateinit var vector: MapPosVector

    var floorElement: VectorElementVector = VectorElementVector()
    var lineElement: VectorElementVector = VectorElementVector()

    /**
     * 공동주택 전개도 레이어
     * @param context Context
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun explodedView(context: Context, source: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        try {
            clear(source)
            polygonArr.clear()

            val elements = VectorElementVector()

            val features = BaseMap.getGeoJsonFeature(context, "test.geojson")
            val total = features?.featureCount!!

            for (i in 0 until total) {

                features.getFeature(i).apply {
                    val geometry = geometry as MultiPolygonGeometry
                    val properties = properties

                    val hoNm: String? = properties.getObjectElement("ho_nm").string
                    val huNum: String? = properties.getObjectElement("hu_num").string
                    val cPoedTxt: String? = properties.getObjectElement("c_poed_txt").string

                    for (j in 0 until geometry.geometryCount) {

                        val createPolygon: Polygon?
                        createPolygon = Polygon(
                            geometry.getGeometry(j),
                            MapStyle.setPolygonStyle(
                                MapElementColor.set(ColorEnum.GREEN),
                                MapElementColor.set(ColorEnum.GREEN),
                                2F
                            )
                        )

                        createPolygon.setMetaDataElement("ho", Variant(hoNm))
                        createPolygon.setMetaDataElement("hu", Variant(huNum))
                        createPolygon.setMetaDataElement("cPoedTxt", Variant(cPoedTxt))

                        polygonArr.add(createPolygon)
                        elements.add(createPolygon)

                        val minusNum = 1.8
                        val centerPos =
                            MapPos(geometry.getGeometry(j).centerPos.x, geometry.getGeometry(j).centerPos.y + minusNum)
                        val middlePos = MapPos(centerPos.x, centerPos.y - minusNum)
                        val botPos = MapPos(middlePos.x, middlePos.y - minusNum)

                        elements.add(
                            Text(
                                middlePos,
                                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.RED), 32F),
                                huNum
                            )
                        )
                        elements.add(
                            Text(
                                botPos,
                                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 30F),
                                cPoedTxt
                            )
                        )
                        elements.add(
                            Text(
                                centerPos,
                                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 30F),
                                hoNm
                            )
                        )
                    }

                }


            }

            MapConst.BASE_EXPLODEDVIEW = polygonArr

            source?.addAll(elements)

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }

    }

    /**
     * 층 올리기
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun addFloor(source: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        setFilterArr(source, polygonArr, "floorUp")

        filterArr?.map {

            south = MapPos(it.bounds.min.x, it.bounds.max.y)
            west = MapPos(it.bounds.max.x, it.bounds.max.y)

            north = MapPos(it.bounds.max.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
            east = MapPos(it.bounds.min.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)

            vector = MapPosVector()
            vector.apply { add(south); add(west); add(north); add(east) }

            addPolygon = Polygon(
                vector,
                MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.BLUE),
                    MapElementColor.set(ColorEnum.BLUE),
                    2F
                )
            )

            val newFloor = increaseFloor(it)
            addPolygon.setMetaDataElement("ho", Variant(newFloor.toString()))

            addText = Text(
                addPolygon.geometry.centerPos,
                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                newFloor.toString()
            )

            polygonArr.add(addPolygon)

            floorElement.add(addPolygon)
            floorElement.add(addText)
        }

        source?.addAll(floorElement)

    }

    /**
     * 라인 추가
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun addLine(source: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        setFilterArr(source, polygonArr, "addLine")

        filterArr?.map {

            south = MapPos(it.bounds.max.x, it.bounds.min.y)
            west = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.min.y)

            north = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.max.y)
            east = MapPos(it.bounds.max.x, it.bounds.max.y)

            vector = MapPosVector()
            vector.apply { add(south); add(west); add(north); add(east) }

            val newLine = increasLine(it)
            addPolygon.setMetaDataElement("ho", Variant(newLine.toString()))

            addPolygon = Polygon(
                vector,
                MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.HOTPINK),
                    MapElementColor.set(ColorEnum.HOTPINK),
                    2F
                )
            )

            addText = Text(
                addPolygon.geometry.centerPos,
                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                newLine.toString()
            )

            polygonArr.add(addPolygon)

            lineElement.add(addPolygon)
            lineElement.add(addText)
        }

        source?.addAll(lineElement)
    }

    /**
     * 최대값 추출
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     * @param type String
     */
    private fun setFilterArr(
        source: LocalVectorDataSource?,
        polygonArr: MutableList<Polygon>,
        type: String
    ) {
        clear(source)
        maxVal = 0
        maxArr.clear()
        filterArr?.clear()

        when (type) {
            "floorUp" -> {
                polygonArr.map { maxArr.add(it.bounds.max.y.toInt()) }
                maxVal = maxArr.max(maxArr)
                filterArr = polygonArr.filter { it.bounds.max.y == maxVal?.toDouble() } as MutableList<Polygon>
            }
            "addLine" -> {
                polygonArr.map { maxArr.add(it.bounds.max.x.toInt()) }
                maxVal = maxArr.max(maxArr)
                filterArr = polygonArr.filter { it.bounds.max.x.toInt() == maxVal } as MutableList<Polygon>

            }
        }

    }

    /**
     * dataSource 초기화
     * @param source LocalVectorDataSource?
     */
    private fun clear(source: LocalVectorDataSource?) { source?.clear() }

    /**
     * 층 추가
     * @param it Polygon
     * @return Int
     */
    private fun increaseFloor(it: Polygon): Int {
        val increaseHo = it.getMetaDataElement("ho").string.toInt() + 100
        BaseMap.addFeatures(it.geometry, increaseHo)
        return it.getMetaDataElement("ho").string.toInt() + 100
    }

    /**
     * 라인 추가
     * @param it Polygon
     * @return Int
     */
    private fun increasLine(it: Polygon): Int {
        val increaseHo = it.getMetaDataElement("ho").string.toInt() + 1
        BaseMap.addFeatures(it.geometry, increaseHo)
        return it.getMetaDataElement("ho").string.toInt() + 1
    }

}