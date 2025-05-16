package com.example.exploedview.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.carto.components.Options
import com.carto.components.RenderProjectionMode
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.MapRange
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.Feature
import com.carto.geometry.FeatureBuilder
import com.carto.geometry.FeatureCollection
import com.carto.geometry.FeatureVector
import com.carto.geometry.GeoJSONGeometryReader
import com.carto.geometry.Geometry
import com.carto.geometry.PointGeometry
import com.carto.geometry.PolygonGeometry
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
import com.carto.projections.Projection
import com.carto.ui.MapView
import com.carto.utils.BitmapUtils
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.VectorElementVector
import com.example.exploedview.MapActivity
import com.example.exploedview.R
import com.example.exploedview.base.BaseException
import com.example.exploedview.map.MapLayer.featureCount
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("StaticFieldLeak")

/**
 * BaseMap
 * 공동주택 전개도 지도를 구성하는 객체
 */

object BaseMap : MapViewModel() {

    private lateinit var context: Context
    lateinit var activity: MapActivity

    // map
    private lateinit var mapView: MapView
    private lateinit var mapOpt: Options
    private lateinit var proj: Projection

    // source
    lateinit var explodedViewSource: LocalVectorDataSource
    lateinit var containsDataSource: LocalVectorDataSource
    lateinit var addFloorDataSource: LocalVectorDataSource
    lateinit var addLineDataSource: LocalVectorDataSource
    lateinit var addHoDataSource: LocalVectorDataSource

    // layer
    val explodedViewLayer: EditableVectorLayer by lazy {
        EditableVectorLayer(
            explodedViewSource
        )
    }

    private val groupLayer: EditableVectorLayer by lazy { EditableVectorLayer(containsDataSource) }
    private val floorUpLayer: EditableVectorLayer by lazy { EditableVectorLayer(addFloorDataSource) }
    private val addLineLayer: EditableVectorLayer by lazy { EditableVectorLayer(addLineDataSource) }
    private val addHoLayer: EditableVectorLayer by lazy { EditableVectorLayer(addHoDataSource) }

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var containsPolygonArr = mutableListOf<Polygon>()
    var selectPolygonArr = mutableListOf<Polygon>()
    var clickPosArr = mutableListOf<MapPos>()

    // listener
    var selectListener: VectorElementSelectEventListener? = null

    // feature
    private var featureCollection: FeatureCollection? = null


    /**
     * 최초 맵 세팅
     * @param mapView MapView
     * @param activity Activity
     * @param context Context
     */
    fun initBaseMap(complexPk: String?, mapView: MapView, activity: Activity, context: Context) {

        this.context = context
        this.activity = activity as MapActivity

        this.mapView = mapView
        mapOpt = this.mapView.options
        proj = mapOpt.baseProjection

        // 맵 옵션
        mapOpt.apply {
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
            backgroundBitmap = BitmapUtils.loadBitmapFromAssets("map_innobrick.png")
            clearColor = MapColor.WHITE
            zoomRange = MapRange(19f, 24.5f)
            renderProjectionMode = RenderProjectionMode.RENDER_PROJECTION_MODE_PLANAR
            tileThreadPoolSize = 2
            watermarkScale = 0.0f
        }
        setInitZoomAndPos(18.toFloat(), MapPos(55.880251, 272.365759), 0.5F)

        explodedViewSource = LocalVectorDataSource(proj)
        containsDataSource = LocalVectorDataSource(proj)
        addFloorDataSource = LocalVectorDataSource(proj)
        addLineDataSource = LocalVectorDataSource(proj)
        addHoDataSource = LocalVectorDataSource(proj)

        val layerArr =
            mutableListOf(explodedViewLayer, groupLayer, floorUpLayer, addLineLayer, addHoLayer)
        setLayer(layerArr)

        CoroutineScope(Dispatchers.Main).launch {

            val job: Deferred<Boolean> = async(Dispatchers.Main) {
                val result =
                    MapLayer.explodedView(activity, complexPk, explodedViewSource, createPolygonArr)
                result
            }

            val getResult = job.await()

            activity.runOnUiThread {
                runCatching {
                    if (!getResult) throw BaseException("전개도 레이어 생성 실패")
                }.fold(
                    onSuccess = { activity.vm.showLoadingBar(false) },
                    onFailure = {
                        LogUtil.e(it.message.toString())

                        // 신규 추가
                        val layoutInflater = activity.layoutInflater
                        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
                        val dialog = AlertDialog.Builder(activity)
                            .setView(dialogView)
                            .setCancelable(true)
                            .show()

                        val tvTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                        val tvMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
                        val btnCancel = dialogView.findViewById<Button>(R.id.dialog_cancel_button)
                        val btnConfirm = dialogView.findViewById<Button>(R.id.dialog_confirm_button)

                        tvTitle.text = "전개도 레이어 생성 실패"

                        // 신규 전개도 추가
                        tvMessage.text = "선택하신 공동주택은 전개도가 존재하지 않습니다.\n" +
                                "신규 전개도를 추가 하시겠습니까?"

                        btnCancel.text = "취소"
                        btnCancel.setOnClickListener {
                            dialog.dismiss()
                            // 뒤로 가기
                            activity.onBackPressed()
                        }

                        btnConfirm.text = "확인"
                        btnConfirm.setOnClickListener {

                            val layoutInflater = activity.layoutInflater
                            val dialogView =
                                layoutInflater.inflate(R.layout.layout_new_map_edit, null)

                            val editDialog = AlertDialog.Builder(activity)
                                .setView(dialogView)
                                .setCancelable(true)
                                .show()

                            val editTextFloor =
                                dialogView.findViewById<EditText>(R.id.edit_text_floor) // 층수

                            val editTextLine =
                                dialogView.findViewById<EditText>(R.id.edit_text_line) // 호수

                            val btnNewMapCancel =
                                dialogView.findViewById<Button>(R.id.dialog_cancel_button) // 취소

                            val btnNewMapConfirm =
                                dialogView.findViewById<Button>(R.id.dialog_confirm_button) // 확인

                            btnNewMapCancel.text = "취소"
                            btnNewMapCancel.setOnClickListener {
                                // 닫기
                                editDialog.dismiss()
                            }

                            btnNewMapConfirm.text = "확인"
                            btnNewMapConfirm.setOnClickListener {

                                val floor = editTextFloor.text.toString()
                                val line = editTextLine.text.toString()

                                if (floor.isEmpty() || line.isEmpty()) {
                                    activity.vm.showErrorMsg("층수와 호수를 입력해주세요.")
                                } else {

                                    dialog.dismiss()

                                    // 신규
                                    val floorNum = floor.toInt()
                                    val lineNum = line.toInt()

                                    // 현재 맵 가운데 좌표
                                    val centerPos = mapView.focusPos!!

                                    var polygon: PolygonGeometry
                                    var newFloorPolygon: Polygon
                                    var newLinePolygon: Polygon?

                                    val newElemetns = VectorElementVector()

                                    // 층수
                                    for (i in 0 until floorNum) {

                                        val addFloor = i * MapConst.INCREASE_FLOOR_NUM

                                        val vertex1 = PointGeometry(
                                            MapPos(
                                                centerPos.x,
                                                centerPos.y + addFloor
                                            )
                                        ) // 0,0
                                        val vertex2 = PointGeometry(
                                            MapPos(
                                                centerPos.x,
                                                centerPos.y + addFloor + MapConst.INCREASE_FLOOR_NUM
                                            )
                                        ) // 0,1
                                        val vertex3 = PointGeometry(
                                            MapPos(
                                                centerPos.x + MapConst.INCREASE_LINE_NUM,
                                                centerPos.y + addFloor + MapConst.INCREASE_FLOOR_NUM
                                            )
                                        ) // 1,1
                                        val vertex4 = PointGeometry(
                                            MapPos(
                                                centerPos.x + MapConst.INCREASE_LINE_NUM,
                                                centerPos.y + addFloor
                                            )
                                        ) // 1,0

                                        // polygon
                                        polygon = PolygonGeometry(
                                            MapPosVector().apply {
                                                add(vertex1.pos)
                                                add(vertex2.pos)
                                                add(vertex3.pos)
                                                add(vertex4.pos)
                                            },
                                        )

                                        newFloorPolygon = Polygon(
                                            polygon,
                                            MapStyle.setPolygonStyle(
                                                MapColor.TEAL, MapColor.TEAL, 2F
                                            ),
                                        )

                                        newFloorPolygon.setMetaDataElement("SELECT", Variant("n"))

                                        MapConst.PROPERTIES_VALUE_ARR.map {
                                            newFloorPolygon.setMetaDataElement(it, Variant(""))
                                        }

                                        newElemetns.add(newFloorPolygon)
                                        createPolygonArr.add(newFloorPolygon)

                                        // 라인 수
                                        for (j in 1 until lineNum) {

                                            val add2 = j * MapConst.INCREASE_LINE_NUM

                                            val vertex5 = PointGeometry(
                                                MapPos(
                                                    centerPos.x + add2,
                                                    centerPos.y + addFloor
                                                )
                                            ) // 0,0
                                            val vertex6 = PointGeometry(
                                                MapPos(
                                                    centerPos.x + add2,
                                                    centerPos.y + addFloor + MapConst.INCREASE_FLOOR_NUM
                                                )
                                            ) // 0,1
                                            val vertex7 = PointGeometry(
                                                MapPos(
                                                    centerPos.x + MapConst.INCREASE_LINE_NUM + add2,
                                                    centerPos.y + addFloor + MapConst.INCREASE_FLOOR_NUM
                                                )
                                            ) // 1,1
                                            val vertex8 = PointGeometry(
                                                MapPos(
                                                    centerPos.x + MapConst.INCREASE_LINE_NUM + add2,
                                                    centerPos.y + addFloor
                                                )
                                            ) // 1,0

                                            // polygon
                                            polygon = PolygonGeometry(
                                                MapPosVector().apply {
                                                    add(vertex5.pos)
                                                    add(vertex6.pos)
                                                    add(vertex7.pos)
                                                    add(vertex8.pos)
                                                },
                                            )

                                            newLinePolygon = Polygon(
                                                polygon,
                                                MapStyle.setPolygonStyle(
                                                    MapColor.TEAL, MapColor.TEAL, 2F
                                                ),
                                            )
                                            newLinePolygon.setMetaDataElement(
                                                "SELECT",
                                                Variant("n")
                                            )

                                            MapConst.PROPERTIES_VALUE_ARR.map {
                                                newLinePolygon.setMetaDataElement(it, Variant(""))
                                            }

                                            newElemetns.add(newLinePolygon)
                                            createPolygonArr.add(newLinePolygon)

                                        }
                                    }

                                    // 라인수
                                    explodedViewSource.addAll(newElemetns)

                                    dialog.dismiss()
                                    editDialog.dismiss()

                                    activity.vm.showSuccessMsg("신규 전개도 추가 완료")

                                }

                            }
                        }

                    }
                )
            }

        }


        this.mapView.mapEventListener =
            MapCustomEventListener(this.mapView, containsDataSource, clickPosArr)
    }

    /**
     * 최초 줌 지정 및 위치 지정
     * @param zoom Float
     * @param pos MapPos
     * @param duration Float
     */
    private fun setInitZoomAndPos(zoom: Float, pos: MapPos, duration: Float) {
        mapView.apply {
            setZoom(zoom, duration)
            setFocusPos(pos, duration)
        }
    }

    /**
     * GeoJson 값 가져오기
     * @param fileName  Asset Package 내 불러올 파일명 (json type)
     * @return FeatureCollection?
     */
    fun getGeoJsonFeature(context: Context, fileName: String): FeatureCollection? {
        val reader: GeoJSONGeometryReader?
        val json: String
        var result: FeatureCollection? = null
        try {
//            val assetManager = context.resources.assets
//            val stream = assetManager.open(fileName)
//            val size = stream.available()
//            val buffer = ByteArray(size)
//            stream.read(buffer)
//            stream.close()
//            json = String(buffer, charset("UTF-8"))
//            //LogUtil.i("response data => $json")
//            reader = GeoJSONGeometryReader()
//            //reader.targetProjection = _proj
//            result = reader.readFeatureCollection(json)

            // todo: 내부 저장소 부르는걸로 변경
            val file = File(context.filesDir, fileName)
            val stream = file.inputStream()
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            json = String(buffer, charset("UTF-8"))
            reader = GeoJSONGeometryReader()
            result = reader.readFeatureCollection(json)

            // todo: 이부분은 나중에 수정해야함
            //createGeoJsonFile(result)

        } catch (e: Exception) {
            LogUtil.e(e.toString())
            activity.vm.showErrorMsg("GeoJson 파일을 읽어오지 못했습니다.")
        }
        return result
    }

    fun createFeatureCollection(polygonGeometries: MutableList<Polygon>): FeatureCollection {
        val featureVector = FeatureVector()

        polygonGeometries.forEach { geometry ->
            val featureBuilder = FeatureBuilder()
            featureBuilder.apply {
                this.geometry = geometry.geometry as PolygonGeometry
                setPropertyValue("propertyKey", Variant("propertyValue")) // 속성 추가
            }

            val feature: Feature = featureBuilder.buildFeature()
            featureVector.add(feature)
        }

        return FeatureCollection(featureVector)
    }

    /**
     * GeoJson 파일 만들기
     */

//    fun createGeoJsonFile(geoJsonFeature: FeatureCollection?) {
//        try {
//            val geoJSONGeometryWriter = GeoJSONGeometryWriter()
//            val json = geoJSONGeometryWriter.writeFeatureCollection(geoJsonFeature)
//            val fileName = "test.geojson"
//            val file = File(context.filesDir, fileName)
//            file.outputStream().use { outputStream ->
//                outputStream.write(json.toByteArray())
//            }
//            LogUtil.i("json file => $file")
//            LogUtil.i("json file => ${file.absolutePath}")
//
//        } catch (e: Exception) {
//            LogUtil.e(e.toString())
//        }
//    }

    /**
     * 레이어 세팅
     */
    private fun setLayer(layers: MutableList<EditableVectorLayer>) {

        var source: LocalVectorDataSource?
        val nameArr = mutableListOf<String>()

        val enums = enumValues<MapLayerName>()

        enums.mapIndexed { index, name ->
            when (name) {
                MapLayerName.EXPLODED_VIEW -> {
                    source = explodedViewSource
                    layers[index] = EditableVectorLayer(source)
                }

                MapLayerName.GROUP -> {
                    source = containsDataSource
                    layers[index] = EditableVectorLayer(source)
                }

                MapLayerName.ADD_FLOOR -> {
                    source = addFloorDataSource
                    layers[index] = EditableVectorLayer(source)
                }

                MapLayerName.ADD_LINE -> {
                    source = addLineDataSource
                    layers[index] = EditableVectorLayer(source)
                }

                MapLayerName.ADD_HO -> {
                    source = addHoDataSource
                    layers[index] = EditableVectorLayer(source)
                }

            }

            setLayerName(layers[index], "name", Variant(name.value))
            mapView.layers.add(layers[index])

        }
            .also {
                for (i in 0 until getLayerCount()) {
                    nameArr.add(mapView.layers.get(i).metaData.get("name").string)
                }
                LogUtil.i("생성된 레이어 이름 : ${nameArr}, 현재 생성된 레이어의 갯수 : ${getLayerCount()}")
                activity.vm.getBaseLayersCount(getLayerCount())
            }
    }


    /**
     * 레이어 명 세팅
     * @param layer VectorLayer
     * @param key String
     * @param name Variant
     */
    private fun setLayerName(layer: Layer?, key: String, name: Variant) =
        layer?.setMetaDataElement(key, name)

    /**
     * 레이어 명 가져오기
     * @param index Int
     * @param key String
     * @return String?
     */
    fun getLayerName(index: Int, key: String): String =
        mapView.layers.get(index).getMetaDataElement(key).string

    /**
     * 레이어의 갯수
     * @return Int
     */
    fun getLayerCount(): Int = mapView.layers.count()

    /**
     * 초기화
     */
    fun clear() {

        CoroutineScope(Dispatchers.Main).launch {
            val job: Deferred<Boolean> = async(Dispatchers.Main) {
                val result = MapLayer.clearLayer(BaseMap, mapView)
                result
            }

            val getResult = job.await()

            activity.runOnUiThread {
                runCatching {
                    if (!getResult) throw BaseException("전개도 레이어 생성 실패")
                }.fold(
                    onSuccess = {
                        activity.vm.showLoadingBar(false); activity.vm.showSuccessMsg(
                        "초기화"
                    )
                    },
                    onFailure = { LogUtil.e(it.message.toString()) }
                )
            }

        }


    }

    /**
     * 부모영역 내 자식 영역 포함 여부 확인 (폴리곤)
     * @param parents MutableList<Polygon>
     * @param child MutableList<Polygon>
     */
    fun contains(parents: MutableList<Polygon>, child: MutableList<Polygon>) {

        val bool = child.isNotEmpty()

        runCatching {
            if (!bool) throw BaseException("그룹영역이 지정되지 않았습니다.")
        }.onSuccess {
            clickPosArr.clear()
            containsDataSource.clear()

            group(parents, child)

        }.onFailure {
            LogUtil.e("group status: $bool, $it")
            activity.vm.showErrorMsg(it.toString())
        }
    }

    /**
     * Feature 추가
     * @param geom Geometry?
     * @param value Int
     */
    fun addFeatures(geom: Geometry?, value: Int) {

        lateinit var newFeature: Feature

        runCatching {
            geom ?: throw BaseException("Feature 타입에 필요한 지오메트리가 존재하지 않습니다.")
        }.onSuccess {
            val featureBuilder = FeatureBuilder()
            featureBuilder.apply {
                geometry = geom as PolygonGeometry
                setPropertyValue("new_yn", Variant("Y"))
                setPropertyValue("HO_NM", Variant(value.toString()))
            }

            newFeature = featureBuilder.buildFeature()

            val featureVector = FeatureVector()
            featureVector.add(newFeature)

            featureCollection = FeatureCollection(featureVector)
            // LogUtil.i("featureCount => ${featureCollection?.featureCount}")

        }.onFailure {
            LogUtil.e(it.toString())
        }.also {
//            val geoJSONGeometryWriter = GeoJSONGeometryWriter()

//            LogUtil.i("NEW_YN => ${newFeature.properties.getObjectElement("new_yn").string}")
//            LogUtil.i("HO_NM =>  ${newFeature.properties.getObjectElement("HO_NM").string}")
//            LogUtil.i("GEOMETRY_BOUNDS => ${newFeature.geometry.bounds}")
//            LogUtil.i("FEATURE_COUNT => ${featureCollection?.featureCount}")

//            LogUtil.i(
//                "FEATURE_COLLECTION => ${
//                    geoJSONGeometryWriter.writeFeatureCollection(
//                        featureCollection
//                    )
//                }"
//            )

        }

    }

    /**
     * 객체 선택 및 비선택
     * @param geometry Geometry
     */
    fun select(geometry: Geometry) {
        createPolygonArr
            .filter { it.geometry == geometry }
            .map {
                when (getPropertiesStringValue(it, "SELECT")) {
                    "n" -> {

                        createPolygonArr.forEach { polygon ->
                            polygon.style = MapStyle.setPolygonStyle(
                                MapColor.TEAL,
                                MapColor.TEAL,
                                2F
                            )
                            polygon.setMetaDataElement("SELECT", Variant("n"))
                        }

                        // 선택은 한개만 적용
                        selectPolygonArr.clear()

                        it.style = MapStyle.setPolygonStyle(
                            MapColor.RED,
                            MapColor.RED,
                            2F
                        )

                        if (activity.binding.switchRead.isChecked) {
                            getPropertiesStringValueArr(it)
                        }

                        if (it.setMetaDataElement("SELECT", Variant("y")) == null) {
                            throw BaseException("선택된 폴리곤의 메타데이터 값이 존재하지 않습니다.")
                        }


                        it.setMetaDataElement("SELECT", Variant("y"))
                        selectPolygonArr.add(it)

                    }

                    "y" -> {
                        it.style = MapStyle.setPolygonStyle(
                            MapColor.TEAL,
                            MapColor.TEAL,
                            2F
                        )
                        it.setMetaDataElement("SELECT", Variant("n"))
                        selectPolygonArr.remove(it)
                    }

                    else -> throw BaseException("잘못된 SELECT EVENT 발생")
                }

            }
            .also {
                activity.runOnUiThread {
                    activity.vm.getSelectExplodedPolygon(selectPolygonArr.size)
                }
            }

        LogUtil.i("선택된 전개도 폴리곤의 개수 : ${selectPolygonArr.size}")
    }

    /**
     * 부모 영역 내 포함된 그룹영역
     * @param parents MutableList<Polygon>
     * @param child MutableList<Polygon>
     */
    private fun group(parents: MutableList<Polygon>, child: MutableList<Polygon>) {
        parents
            .filter { child.contains(it) }
            .map {
                it.style = MapStyle.setPolygonStyle(
                    MapColor.PURPLE,
                    MapColor.PURPLE,
                    2F
                )
            }
    }

    /**
     * 폴리곤 내 프로퍼티 값 세팅하기
     * @param properties Variant
     * @param arr ArrayList<String>
     * @param polygon Polygon
     */
    fun setPropertiesStringValue(properties: Variant, arr: ArrayList<String>, polygon: Polygon) {
        arr.map {
            val getValue = properties.getObjectElement(it).string
            polygon.setMetaDataElement(it, Variant(getValue))
        }
    }

    fun getPropertiesStringValue(polygon: Polygon, value: String): String {
        return polygon.getMetaDataElement(value).string
    }

    private fun getPropertiesStringValueArr(polygon: Polygon) {

        var result = ""

        for ((key, value) in MapConst.PROPERTIES_VALUE_MAP) {
            result += "$key : ${polygon.metaData.get(value).string} \n"
        }

        val arr = ArrayList<String>() // alert data
        arr.run {
            add("레이어 정보")
            add(result)
        }

        activity.runOnUiThread {
//            val isLayerShowToggle = MapEvent.SetLayerReadStatus(activity.binding.switchRead.isChecked).status
//
//            runCatching {
//                if (!isLayerShowToggle) throw BaseException("레이어 View 상태값 : False")
//            }.fold(
//                onSuccess = { activity.vm.showAlertDialog(arr) },
//                onFailure = { LogUtil.e(it.toString()) }
//            )

            val isLayerShow = activity.binding.switchRead.isChecked

            if (isLayerShow) {
                activity.vm.showAlertDialog(arr)
            } else {
                LogUtil.e("레이어 View 상태값 : False")
            }

        }
    }

    fun getPolygonElementCnt(source: LocalVectorDataSource?): Int {
        var resultCnt = 0
        for (i in 0 until source!!.all.size()) {
            when (source.all[i.toInt()]) {
                is Polygon -> resultCnt++
            }
        }
        LogUtil.i("source.all.size() => $resultCnt")

        featureCount += resultCnt
        activity.binding.txtTotal.text = featureCount.toString()

        return resultCnt
    }


}