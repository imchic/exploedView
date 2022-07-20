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
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.extension.Extensions.max
import com.example.exploedview.extension.Extensions.min
import com.example.exploedview.util.LogUtil

object MapLayer {

    private var maxArr = ArrayList<Double>()
    private var params: Double? = null
    private var filterArr: MutableList<Polygon>? = null

    private lateinit var south: MapPos
    private lateinit var west: MapPos
    private lateinit var north: MapPos
    private lateinit var east: MapPos

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

                        val south = MapPos(geometry.getGeometry(j).bounds.min.x, geometry.getGeometry(j).bounds.min.y)
                        val west = MapPos(geometry.getGeometry(j).bounds.max.x, geometry.getGeometry(j).bounds.min.y)

                        val north = MapPos(geometry.getGeometry(j).bounds.max.x, geometry.getGeometry(j).bounds.max.y)
                        val east = MapPos(geometry.getGeometry(j).bounds.min.x, geometry.getGeometry(j).bounds.max.y)

                        val explodedVector = MapPosVector()
                        explodedVector.apply { add(south); add(west); add(north); add(east) }

                        val createPolygon: Polygon?
                        createPolygon = Polygon(
                            explodedVector,
                            MapStyle.setPolygonStyle(
                                MapElementColor.set(ColorEnum.GREEN),
                                MapElementColor.set(ColorEnum.GREEN),
                                2F
                            )
                        )

                        createPolygon.setMetaDataElement("ho", Variant(hoNm))
                        createPolygon.setMetaDataElement("hu", Variant(huNum))
                        createPolygon.setMetaDataElement("cPoedTxt", Variant(cPoedTxt))
                        createPolygon.setMetaDataElement("select", Variant("n"))

                        polygonArr.add(createPolygon)
                        elements.add(createPolygon)

//                        LogUtil.d("min => ${createPolygon.geometry.bounds.min}")
//                        LogUtil.d("max => ${createPolygon.geometry.bounds.max}")

//                        LogUtil.d("min => ${createPolygon.geometry.bounds.min}")
//                        LogUtil.d("max => ${createPolygon.geometry.bounds.max}")

                        val centerPos = MapPos(createPolygon.geometry.centerPos.x, createPolygon.geometry.centerPos.y + 230000)
                        val middlePos = MapPos(createPolygon.geometry.centerPos.x, createPolygon.geometry.centerPos.y - 50000)
                        val botPos = MapPos(createPolygon.geometry.centerPos.x, middlePos.y- 300000)

                        elements.add(
                            Text(
                                centerPos,
                                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 30F),
                                hoNm
                            )
                        )
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
                    }

                }


            }

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

        setFilterArr(source, polygonArr, MapLayerName.ADD_FLOOR)

        filterArr?.map {

            south = MapPos(it.bounds.min.x, it.bounds.max.y)
            west = MapPos(it.bounds.max.x, it.bounds.max.y)

            north = MapPos(it.bounds.max.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
            east = MapPos(it.bounds.min.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)

            val floorVector = MapPosVector()
            floorVector.apply { add(south); add(west); add(north); add(east) }

            val addFloor = Polygon(
                floorVector,
                MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.BLUE),
                    MapElementColor.set(ColorEnum.BLUE),
                    2F
                )
            )

            val newFloor = increaseFloor(it)
            addFloor.setMetaDataElement("ho", Variant(newFloor.toString()))

            val newFloorText = Text(
                addFloor.geometry.centerPos,
                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                newFloor.toString()
            )

            polygonArr.add(addFloor)

            floorElement.add(addFloor)
            floorElement.add(newFloorText)
        }

        source?.addAll(floorElement)

    }

    /**
     * 라인 추가
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun addLine(source: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        setFilterArr(source, polygonArr, MapLayerName.ADD_LINE)

        filterArr?.map {

            south = MapPos(it.bounds.max.x, it.bounds.min.y)
            west = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM,  it.bounds.min.y)

            north = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.max.y)
            east = MapPos(it.bounds.max.x, it.bounds.max.y)

            val lineVector = MapPosVector()
            lineVector.apply { add(south); add(west); add(north); add(east) }

            val addLine = Polygon(
                lineVector,
                MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.HOTPINK),
                    MapElementColor.set(ColorEnum.HOTPINK),
                    2F
                )
            )

            val newLine = increasLine(it)
            addLine.setMetaDataElement("ho", Variant(newLine.toString()))

            val lineText = Text(
                addLine.geometry.centerPos,
                MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                newLine.toString()
            )

            polygonArr.add(addLine)

            lineElement.add(addLine)
            lineElement.add(lineText)
        }

        source?.addAll(lineElement)
    }

    /**
     * 호실 추가
     * @param source LocalVectorDataSource?
     */
    fun addHo(activity: MapActivity, source: LocalVectorDataSource?) {
        runCatching {
            BaseMap.selectPolygonArr.size == 1
        }.onSuccess {
            activity.run {
                if (!it) {
                    activity.showToast("한개의 호실을 선택해주세요.")
                } else {
                    showToast("통과.")
                }
            }
        }.onFailure {
            throw BaseException("add Ho Event 에러 발생")
        }
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
        type: MapLayerName
    ) {
        clear(source)
        maxArr.clear()
        filterArr?.clear()

        params = 0.0

        when (type) {
            MapLayerName.ADD_FLOOR -> {
                polygonArr.map { maxArr.add(it.bounds.max.y) }
                params = maxArr.max(maxArr)
                filterArr = polygonArr.filter { it.bounds.max.y == params } as MutableList<Polygon>
            }
            MapLayerName.ADD_LINE -> {
                polygonArr.map { maxArr.add(it.bounds.max.x) }
                params = maxArr.max(maxArr)
                filterArr = polygonArr.filter { it.bounds.max.x == params } as MutableList<Polygon>

            }
            else -> {}
        }

    }

    /**
     * dataSource 초기화
     * @param source LocalVectorDataSource?
     */
    private fun clear(source: LocalVectorDataSource?) {
        source?.clear()
    }

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