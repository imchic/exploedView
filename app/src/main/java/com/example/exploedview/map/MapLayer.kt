package com.example.exploedview.map

import android.app.Activity
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.Geometry
import com.carto.ui.MapView
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.carto.vectorelements.VectorElementVector
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.extension.getMaxValue
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor

object MapLayer {

    private var posArr = ArrayList<Double>()
    private var params: Double? = null
    private var filterArr: MutableList<Polygon>? = null

    private lateinit var south: MapPos
    private lateinit var west: MapPos
    private lateinit var north: MapPos
    private lateinit var east: MapPos

    private var floorElement: VectorElementVector = VectorElementVector()
    private var lineElement: VectorElementVector = VectorElementVector()
    private var hoElement: VectorElementVector = VectorElementVector()

    var featureCount = 0

    /**
     * 공동주택 전개도 레이어
     * @param context Context
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun explodedView(
        context: Activity,
        source: LocalVectorDataSource?,
        polygonArr: MutableList<Polygon>,
    ): Boolean {

        var flag: Boolean

        try {
            clear(source)
            polygonArr.clear()

            val elements = VectorElementVector()
            val features = BaseMap.getGeoJsonFeature(context, "dusan.geojson")

            if (features != null) {

                val total = features.featureCount
                (context as MapActivity).binding.txtTotal.text = total.toString()

                featureCount = total

                for (i in 0 until total) {

                    features.getFeature(i).apply {
                        val geometry = features.getFeature(i).geometry as Geometry
                        val properties = properties

                        val south = MapPos(geometry.bounds.min.x, geometry.bounds.min.y)
                        val west = MapPos(geometry.bounds.max.x, geometry.bounds.min.y)

                        val north = MapPos(geometry.bounds.max.x, geometry.bounds.max.y)
                        val east = MapPos(geometry.bounds.min.x, geometry.bounds.max.y)

                        val explodedVector = MapPosVector()
                        explodedVector.apply { add(south); add(west); add(north); add(east) }

                        val createPolygon: Polygon?
                        createPolygon = Polygon(
                            explodedVector,
                            MapStyle.setPolygonStyle(
                                MapColor.TEAL,
                                MapColor.TEAL,
                                2F
                            )
                        )

                        createPolygon.run {
                            BaseMap.setPropertiesStringValue(
                                properties,
                                MapConst.PROPERTIES_VALUE_ARR,
                                this
                            )
                            setMetaDataElement("SELECT", Variant("n"))
                            setMetaDataElement("CUSTOM_INDEX", Variant(i.toString()))

                            polygonArr.add(this)
                            elements.add(this)
                        }
                        val minusNum = 2

                        val centerPos = MapPos(
                            createPolygon.geometry.centerPos.x,
                            createPolygon.geometry.centerPos.y
                        )
                        val middlePos = MapPos(
                            createPolygon.geometry.centerPos.x,
                            createPolygon.geometry.centerPos.y - minusNum
                        )

                        elements.add(
                            Text(
                                centerPos,
                                MapStyle.setTextStyle(MapColor.BLACK, MapConst.FONT_SIZE),
                                BaseMap.getPropertiesStringValue(createPolygon, "HO_NM")
                            )
                        )
                        elements.add(
                            Text(
                                middlePos,
                                MapStyle.setTextStyle(MapColor.RED, MapConst.FONT_SIZE),
                                BaseMap.getPropertiesStringValue(createPolygon, "HU_NUM")
                            )
                        )

                    }


                }

                source?.addAll(elements)
                flag = true
            } else {
                flag = false
            }


        } catch (e: BaseException) {
            LogUtil.e(e.toString())
            flag = false
        }

        return flag

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
                    MapColor.BLUE,
                    MapColor.BLUE,
                    2F
                )
            )

            val newFloor = increaseFloor(it)
            this.addText(addFloor, newFloor, polygonArr, floorElement)
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
            west = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.min.y)

            north = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.max.y)
            east = MapPos(it.bounds.max.x, it.bounds.max.y)

            val lineVector = MapPosVector()
            lineVector.apply { add(south); add(west); add(north); add(east) }

            val addLine = Polygon(
                lineVector,
                MapStyle.setPolygonStyle(
                    MapColor.HOTPINK,
                    MapColor.HOTPINK,
                    2F
                )
            )

            val newLine = increaseLine(it)
            this.addText(addLine, newLine, polygonArr, lineElement)
        }

        source?.addAll(lineElement)
    }

    private fun addText(
        polygon: Polygon,
        value: Int,
        polygonArr: MutableList<Polygon>,
        element: VectorElementVector,
    ) {
        polygon.setMetaDataElement("HO_NM", Variant(value.toString()))
        polygon.setMetaDataElement("SELECT", Variant("n"))

        val addHoText = Text(
            polygon.geometry.centerPos,
            MapStyle.setTextStyle(MapColor.BLACK, MapConst.FONT_SIZE),
            value.toString()
        )

        polygonArr.add(polygon)

        element.add(polygon)
        element.add(addHoText)
    }

    /**
     * 호실 추가 Alert
     * @param activity MapActivity
     */
    fun addHoAlert(activity: MapActivity) {

        val arr = ArrayList<String>()
        arr.add("호실 추가")
        arr.add("왼쪽")
        arr.add("오른쪽")
        arr.add("위로")
        arr.add("아래로")

        activity.vm.showAlertListDialog(arr)
    }

    /**
     * 호실 추가
     * @param source LocalVectorDataSource?
     */
    fun addHo(source: LocalVectorDataSource?, pos: Int) {
        runCatching {
            BaseMap.selectPolygonArr.size

        }.onSuccess {
            when {
                it == 0 -> BaseMap.activity.vm.showSnackbarString("선택된 호실이 없습니다.")
                it > 1 -> BaseMap.activity.vm.showSnackbarString("하나의 호실만 선택해주세요.")

                else -> {
                    val target = BaseMap.selectPolygonArr[0]

                    /**
                     * 호실 추가
                     * 좌, 우, 상, 하 체크 이후 Polygon 생성하게끔 해야함.
                     */

                    target.apply {
                        when (pos) {
                            0 -> {
                                south =
                                    MapPos(bounds.min.x - MapConst.INCREASE_LINE_NUM, bounds.min.y)
                                west =
                                    MapPos(bounds.max.x - MapConst.INCREASE_LINE_NUM, bounds.min.y)

                                north =
                                    MapPos(bounds.max.x - MapConst.INCREASE_LINE_NUM, bounds.max.y)
                                east =
                                    MapPos(bounds.min.x - MapConst.INCREASE_LINE_NUM, bounds.max.y)
                            }

                            1 -> {
                                south = MapPos(bounds.max.x, bounds.min.y)
                                west =
                                    MapPos(bounds.max.x + MapConst.INCREASE_LINE_NUM, bounds.min.y)

                                north =
                                    MapPos(bounds.max.x + MapConst.INCREASE_LINE_NUM, bounds.max.y)
                                east = MapPos(bounds.max.x, bounds.max.y)
                            }

                            2 -> {
                                south = MapPos(bounds.min.x, bounds.max.y)
                                west = MapPos(bounds.max.x, bounds.max.y)

                                north =
                                    MapPos(bounds.max.x, bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
                                east =
                                    MapPos(bounds.min.x, bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
                            }

                            3 -> {
                                south =
                                    MapPos(bounds.min.x, bounds.min.y - MapConst.INCREASE_FLOOR_NUM)
                                west =
                                    MapPos(bounds.max.x, bounds.min.y - MapConst.INCREASE_FLOOR_NUM)

                                north =
                                    MapPos(bounds.max.x, bounds.max.y - MapConst.INCREASE_FLOOR_NUM)
                                east =
                                    MapPos(bounds.min.x, bounds.max.y - MapConst.INCREASE_FLOOR_NUM)
                            }
                        }
                    }

                    val hoVector = MapPosVector()
                    hoVector.apply { add(south); add(west); add(north); add(east) }

                    val addHo = Polygon(
                        hoVector,
                        MapStyle.setPolygonStyle(
                            MapColor.NAVY,
                            MapColor.NAVY,
                            2F
                        )
                    )

                    hoElement.add(addHo)
                    source?.addAll(hoElement)
                }
            }
        }.onFailure {
            LogUtil.e(it.toString())
        }.recover { error ->
            {
                LogUtil.e(error.message.toString())
            }
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
        type: MapLayerName,
    ) {
        clear(source)
        posArr.clear()
        filterArr?.clear()

        params = 0.0

        when (type) {
            MapLayerName.ADD_FLOOR -> {
                polygonArr.map { posArr.add(it.bounds.max.y) }
                params = posArr.getMaxValue()
                filterArr = polygonArr.filter { it.bounds.max.y == params } as MutableList<Polygon>
            }

            MapLayerName.ADD_LINE -> {
                polygonArr.map { posArr.add(it.bounds.max.x) }
                params = posArr.getMaxValue()
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
        val increaseHo = it.getMetaDataElement("HO_NM").string.toInt() + 100
        BaseMap.addFeatures(it.geometry, increaseHo)
        return it.getMetaDataElement("HO_NM").string.toInt() + 100
    }

    /**
     * 라인 추가
     * @param it Polygon
     * @return Int
     */
    private fun increaseLine(it: Polygon): Int {
        val increaseHo = it.getMetaDataElement("HO_NM").string.toInt() + 1
        BaseMap.addFeatures(it.geometry, increaseHo)
        return it.getMetaDataElement("HO_NM").string.toInt() + 1
    }

    /**
     * MapLayer 초기화
     * @param baseMap BaseMap
     * @param mapView MapView
     * @return Boolean
     */
    fun clearLayer(baseMap: BaseMap, mapView: MapView): Boolean {

        return try {
            baseMap.clickPosArr.clear()

            baseMap.containsDataSource.clear()
            baseMap.addFloorDataSource.clear()
            baseMap.addLineDataSource.clear()
            baseMap.addHoDataSource.clear()

            floorElement.clear()
            lineElement.clear()
            hoElement.clear()

            baseMap.explodedViewSource.clear()
            explodedView(baseMap.activity, baseMap.explodedViewSource, baseMap.createPolygonArr)

            baseMap.selectPolygonArr.clear()
            baseMap.containsPolygonArr.clear()

            baseMap.activity.vm.run {
                getTotalExplodedPolygon(baseMap.createPolygonArr.size)
                getBaseLayersCount(mapView.layers.count())
                getSelectExplodedPolygon(baseMap.selectPolygonArr.size)
                getGroupExplodedPolygon(baseMap.containsPolygonArr.size)
                getCoordinates("0,0")
                getAddFloorValue(baseMap.addFloorDataSource.all!!.size().toInt())
                getAddLIneValue(baseMap.addLineDataSource.all!!.size().toInt())
                getAddHo(baseMap.addHoDataSource.all!!.size().toInt())
                getContains(baseMap.containsDataSource.all!!.size().toInt())
            }

            true

        } catch (e: Exception) {
            false
        }

    }

}