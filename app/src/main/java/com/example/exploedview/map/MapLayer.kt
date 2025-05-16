package com.example.exploedview.map

import android.app.Activity
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.Geometry
import com.carto.layers.VectorLayer
import com.carto.ui.MapView
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.carto.vectorelements.VectorElementVector
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.extension.getMaxValue
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private var defaultLayer: VectorLayer? = null

    var featureCount = 0

    /**
     * 공동주택 전개도 레이어
     * @param context Context
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    suspend fun explodedView(
        context: Activity,
        complexPk: String?,
        source: LocalVectorDataSource?,
        polygonArr: MutableList<Polygon>,
    ): Boolean {

        var flag: Boolean

        try {
            clear(source)
            polygonArr.clear()

            val elements = VectorElementVector()
            //val features = BaseMap.getGeoJsonFeature(context, "dusan.geojson")
            // await

            val features = withContext(Dispatchers.IO) {
                BaseMap.getGeoJsonFeature(context, "${complexPk}.geojson")
            }

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
                            explodedVector, MapStyle.setPolygonStyle(
                                MapColor.TEAL, MapColor.TEAL, 2F
                            )
                        )

                        createPolygon.run {
                            BaseMap.setPropertiesStringValue(
                                properties, MapConst.PROPERTIES_VALUE_ARR, this
                            )
                            setMetaDataElement("SELECT", Variant("n"))
                            setMetaDataElement("CUSTOM_INDEX", Variant(i.toString()))

                            polygonArr.add(this)
                            elements.add(this)
                        }
                        val minusNum = 2

                        val centerPos = MapPos(
                            createPolygon.geometry.centerPos.x, createPolygon.geometry.centerPos.y
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
//                        elements.add(
//                            Text(
//                                middlePos,
//                                MapStyle.setTextStyle(MapColor.RED, MapConst.FONT_SIZE),
//                                BaseMap.getPropertiesStringValue(createPolygon, "HU_NUM")
//                            )
//                        )

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
     * @param addFloorSource LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun addFloor(addFloorSource: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        val selectPolygon = BaseMap.selectPolygonArr

        if (selectPolygon.size == 0) {
            BaseMap.activity.vm.showWarningMsg("선택된 폴리곤이 없습니다.")
            return
        }

        var targetMaxY = 0.0
        var targetMinY = 0.0

        var targetMaxX = 0.0
        var targetMinX = 0.0

        val insertFloorFilterArr = mutableListOf<Polygon>()
        val targetMoreBiggerMaxArr = mutableListOf<Polygon>() // 타겟보다 큰 폴리곤의 maxY 배열
        val targetLessrMinArr = mutableListOf<Polygon>() // 타겟보다 작은 폴리곤의 maxY 배열
        val modifyFloorFilterArr = mutableListOf<Polygon>() // 중간층 삽입 이후 전체 폴리곤 배열

        var range = 0
        var pos = 0

        MaterialAlertDialogBuilder(BaseMap.activity).setTitle("층 추가").setCancelable(false)
            .setSingleChoiceItems(
                arrayOf("위로", "아래로"), 0
            ) { _, which -> pos = which }.setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                try {

                    selectPolygon.map {
                        targetMaxY = it.bounds.max.y
                        targetMinY = it.bounds.min.y

                        targetMaxX = it.bounds.max.x
                        targetMinX = it.bounds.min.x

                        val targetMoreBiggerMax = polygonArr.filter { it.bounds.max.y > targetMaxY }
                        val targetLessrMin = polygonArr.filter { it.bounds.min.y < targetMinY }

                        targetMoreBiggerMaxArr.addAll(targetMoreBiggerMax)
                        targetLessrMinArr.addAll(targetLessrMin)
                    }

                    range = 1
                    LogUtil.i("targetMoreBiggerMaxArr : ${targetMoreBiggerMaxArr.size}")

                    val createPolygon = BaseMap.createPolygonArr
                    LogUtil.i("createPolygonSize : ${createPolygon.size}")

                    createPolygon.map {

                        // 상
                        if (Math.round(it.bounds.max.y).toDouble() == Math.round(targetMaxY)
                                .toDouble()
                        ) {
                            targetMaxX = it.bounds.max.y
                            insertFloorFilterArr.add(it)
                        }

                        // 하
                        if (Math.round(it.bounds.min.y).toDouble() == Math.round(targetMinY)
                                .toDouble()
                        ) {
                            targetMinY = it.bounds.min.y
                            insertFloorFilterArr.add(it)
                        }

                        // 그 이후 층
                        if (Math.round(it.bounds.max.y).toDouble() > Math.round(targetMaxY)
                                .toDouble()
                        ) {
                            targetMaxX = it.bounds.max.y
                            modifyFloorFilterArr.add(it)
                        }
                    }

                    LogUtil.i("insertFloorFilterArr : ${insertFloorFilterArr.size}")

                    when (pos) {
                        0 -> {

                            insertFloorFilterArr.map {

                                south = MapPos(it.bounds.min.x, it.bounds.max.y)
                                west = MapPos(it.bounds.max.x, it.bounds.max.y)

                                north = MapPos(
                                    it.bounds.max.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM
                                )
                                east = MapPos(
                                    it.bounds.min.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM
                                )

                                val floorVector = MapPosVector()
                                floorVector.apply { add(south); add(west); add(north); add(east) }

                                val addFloor = Polygon(
                                    floorVector, MapStyle.setPolygonStyle(
                                        MapColor.BROWN, MapColor.BROWN, 2F
                                    )
                                )

                                val newFloor = increaseFloor(it)
                                this.addText(addFloor, newFloor, polygonArr, floorElement)

                            }

                            modifyFloorFilterArr.map {

                                south = MapPos(it.bounds.min.x, it.bounds.max.y)
                                west = MapPos(it.bounds.max.x, it.bounds.max.y)

                                north = MapPos(
                                    it.bounds.max.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM
                                )
                                east = MapPos(
                                    it.bounds.min.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM
                                )

                                val floorVector = MapPosVector()
                                floorVector.apply { add(south); add(west); add(north); add(east) }

                                val addFloor = Polygon(
                                    floorVector, MapStyle.setPolygonStyle(
                                        MapColor.TEAL, MapColor.TEAL, 2F
                                    )
                                )

                                val newFloor = increaseFloor(it)
                                this.addText(addFloor, newFloor, polygonArr, floorElement)

                                BaseMap.explodedViewSource.add(addFloor)

                            }


                            val newElementList = VectorElementVector()
                            val bluePrintViewSource = BaseMap.explodedViewSource.all

                            for (i in 0 until bluePrintViewSource.size()) {
                                val element = bluePrintViewSource.get(i.toInt())

                                var newPolygon: Polygon? = null

                                if (element is Polygon) {
                                    for (j in 0 until insertFloorFilterArr.size) {
                                        val insertElement = insertFloorFilterArr[j]

                                        if (element.geometry == insertElement.geometry) {
                                            BaseMap.explodedViewSource.remove(element)

                                            // element를 복사해서 새로운 Polygon 생성
                                            newPolygon = Polygon(
                                                element.geometry,
                                                MapStyle.setPolygonStyle(
                                                    MapColor.BLUE,
                                                    MapColor.BLUE,
                                                    2F
                                                )
                                            )

                                            LogUtil.i("element : ${element.bounds.max.y}")

                                            newElementList.add(newPolygon)
                                            break
                                        }
                                    }
                                }
                            }

                            BaseMap.explodedViewSource.addAll(newElementList)
                        }

                        1 -> {
                            insertFloorFilterArr.map {

                                south = MapPos(it.bounds.min.x, it.bounds.min.y)
                                west = MapPos(it.bounds.max.x, it.bounds.min.y)

                                north = MapPos(
                                    it.bounds.max.x, it.bounds.min.y - MapConst.INCREASE_FLOOR_NUM
                                )
                                east = MapPos(
                                    it.bounds.min.x, it.bounds.min.y - MapConst.INCREASE_FLOOR_NUM
                                )

                                val floorVector = MapPosVector()
                                floorVector.apply { add(south); add(west); add(north); add(east) }

                                val addFloor = Polygon(
                                    floorVector, MapStyle.setPolygonStyle(
                                        MapColor.RED, MapColor.RED, 2F
                                    )
                                )

                                val newFloor = increaseFloor(it)
                                this.addText(addFloor, newFloor, polygonArr, floorElement)

                            }

                            modifyFloorFilterArr.map {

                                south = MapPos(it.bounds.min.x, it.bounds.min.y)
                                west = MapPos(it.bounds.max.x, it.bounds.min.y)

                                north = MapPos(
                                    it.bounds.max.x, it.bounds.min.y - MapConst.INCREASE_FLOOR_NUM
                                )
                                east = MapPos(
                                    it.bounds.min.x, it.bounds.min.y - MapConst.INCREASE_FLOOR_NUM
                                )

                                val floorVector = MapPosVector()
                                floorVector.apply { add(south); add(west); add(north); add(east) }

                                val addFloor = Polygon(
                                    floorVector, MapStyle.setPolygonStyle(
                                        MapColor.TEAL, MapColor.TEAL, 2F
                                    )
                                )

                                val newFloor = increaseFloor(it)
                                this.addText(addFloor, newFloor, polygonArr, floorElement)

                            }

                            val newElementList = VectorElementVector()
                            val bluePrintViewSource = BaseMap.explodedViewSource.all

                            for (i in 0 until bluePrintViewSource.size()) {
                                val element = bluePrintViewSource.get(i.toInt())

                                var newPolygon: Polygon? = null

                                if (element is Polygon) {
                                    for (j in 0 until insertFloorFilterArr.size) {
                                        val insertElement = insertFloorFilterArr[j]

                                        if (element.geometry == insertElement.geometry) {
                                            BaseMap.explodedViewSource.remove(element)

                                            // element를 복사해서 새로운 Polygon 생성
                                            newPolygon = Polygon(
                                                element.geometry,
                                                MapStyle.setPolygonStyle(
                                                    MapColor.BLUE,
                                                    MapColor.BLUE,
                                                    2F
                                                )
                                            )

                                            LogUtil.i("element : ${element.bounds.max.y}")

                                            newElementList.add(newPolygon)
                                            break
                                        }
                                    }
                                }
                            }

                            BaseMap.explodedViewSource.addAll(newElementList)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.show()


//        setFilterArr(source, polygonArr, MapLayerName.ADD_FLOOR)

//        filterArr?.map {
//
//            south = MapPos(it.bounds.min.x, it.bounds.max.y)
//            west = MapPos(it.bounds.max.x, it.bounds.max.y)
//
//            north = MapPos(it.bounds.max.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
//            east = MapPos(it.bounds.min.x, it.bounds.max.y + MapConst.INCREASE_FLOOR_NUM)
//
//            val floorVector = MapPosVector()
//            floorVector.apply { add(south); add(west); add(north); add(east) }
//
//            val addFloor = Polygon(
//                floorVector,
//                MapStyle.setPolygonStyle(
//                    MapColor.BLUE,
//                    MapColor.BLUE,
//                    2F
//                )
//            )
//
//            val newFloor = increaseFloor(it)
//            this.addText(addFloor, newFloor, polygonArr, floorElement)
//        }
//
//        source?.addAll(floorElement)

    }

    /**
     * 라인 추가
     * @param source LocalVectorDataSource?
     * @param polygonArr MutableList<Polygon>
     */
    fun addLine(source: LocalVectorDataSource?, polygonArr: MutableList<Polygon>) {

        val selectPolygon = BaseMap.selectPolygonArr

        if (selectPolygon.size == 0) {
            BaseMap.activity.vm.showErrorMsg("선택된 폴리곤이 없습니다.")
            return
        }

        setFilterArr(source, polygonArr, MapLayerName.ADD_LINE)

        filterArr?.map {

            south = MapPos(it.bounds.max.x, it.bounds.min.y)
            west = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.min.y)

            north = MapPos(it.bounds.max.x + MapConst.INCREASE_LINE_NUM, it.bounds.max.y)
            east = MapPos(it.bounds.max.x, it.bounds.max.y)

            val lineVector = MapPosVector()
            lineVector.apply { add(south); add(west); add(north); add(east) }

            val addLine = Polygon(
                lineVector, MapStyle.setPolygonStyle(
                    MapColor.HOTPINK, MapColor.HOTPINK, 2F
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

        activity.vm.showAlertSelectDialog(arr)
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
                it == 0 -> BaseMap.activity.vm.showWarningMsg("선택된 호실이 없습니다.")
                it > 1 -> BaseMap.activity.vm.showWarningMsg("하나의 호실만 선택해주세요.")

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
                        hoVector, MapStyle.setPolygonStyle(
                            MapColor.NAVY, MapColor.NAVY, 2F
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

        var increaseHo: Int

        val hoNm = it.getMetaDataElement("HO_NM").string

        if (hoNm.isEmpty() || !hoNm.all { char -> char.isDigit() }) {
            it.setMetaDataElement("HO_NM", Variant("0"))
            return 100
        } else {
            increaseHo = hoNm.toInt() + 100
            BaseMap.addFeatures(it.geometry, increaseHo)
            return increaseHo
        }
    }

    /**
     * 라인 추가
     * @param it Polygon
     * @return Int
     */
    private fun increaseLine(it: Polygon): Int {
        var increaseLine: Int

        val hoNm = it.getMetaDataElement("HO_NM").string

        if (hoNm.isEmpty() || !hoNm.all { char -> char.isDigit() }) {
            it.setMetaDataElement("HO_NM", Variant("0"))
            return 0
        } else {
            increaseLine = hoNm.toInt() + 1
            BaseMap.addFeatures(it.geometry, increaseLine)
            return increaseLine
        }
    }

    /**
     * MapLayer 초기화
     * @param baseMap BaseMap
     * @param mapView MapView
     * @return Boolean
     */
    suspend fun clearLayer(baseMap: BaseMap, mapView: MapView): Boolean {

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
            explodedView(
                baseMap.activity,
                baseMap.activity.vm.complexPk.value,
                baseMap.explodedViewSource,
                baseMap.createPolygonArr
            )

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