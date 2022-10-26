package com.example.exploedview.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.carto.components.Options
import com.carto.core.MapPos
import com.carto.core.MapRange
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.*
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
import com.carto.projections.Projection
import com.carto.ui.MapView
import com.carto.vectorelements.Polygon
import com.example.exploedview.MapActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.util.LogUtil
import com.example.exploedview.util.MapColor
import kotlinx.coroutines.*

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
    private val explodedViewLayer: EditableVectorLayer by lazy { EditableVectorLayer(explodedViewSource) }
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
    fun initBaseMap(mapView: MapView, activity: Activity, context: Context) {

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
//            backgroundBitmap = BitmapUtils.loadBitmapFromAssets("ci.png")
        }
        setInitZoomAndPos(18.toFloat(), MapPos(55.880251, 272.365759), 0.5F)

        explodedViewSource = LocalVectorDataSource(proj)
        containsDataSource = LocalVectorDataSource(proj)
        addFloorDataSource = LocalVectorDataSource(proj)
        addLineDataSource = LocalVectorDataSource(proj)
        addHoDataSource = LocalVectorDataSource(proj)

        val layerArr = mutableListOf(explodedViewLayer, groupLayer, floorUpLayer, addLineLayer, addHoLayer)
        setLayer(layerArr)

        CoroutineScope(Dispatchers.Main).launch {

            val job: Deferred<Boolean> = async(Dispatchers.Main) {
                val result = MapLayer.explodedView(activity, explodedViewSource, createPolygonArr)
                result
            }

            val getResult = job.await()

            activity.runOnUiThread {
                runCatching {
                    if(!getResult) throw BaseException("전개도 레이어 생성 실패")
                }.fold(
                    onSuccess = { activity.vm.showLoadingBar(false) },
                    onFailure = { LogUtil.e(it.message.toString()) }
                )
            }

        }


        this.mapView.mapEventListener = MapCustomEventListener(this.mapView, containsDataSource, clickPosArr)
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
            val assetManager = context.resources.assets
            val stream = assetManager.open(fileName)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            json = String(buffer, charset("UTF-8"))
            //LogUtil.i("response data => $json")
            reader = GeoJSONGeometryReader()
            //reader.targetProjection = _proj
            result = reader.readFeatureCollection(json)
        } catch (e: Exception) {
            LogUtil.e(e.toString())
        }
        return result
    }

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
    private fun setLayerName(layer: Layer?, key: String, name: Variant) = layer?.setMetaDataElement(key, name)

    /**
     * 레이어 명 가져오기
     * @param index Int
     * @param key String
     * @return String?
     */
    fun getLayerName(index: Int, key: String): String = mapView.layers.get(index).getMetaDataElement(key).string

    /**
     * 레이어의 갯수
     * @return Int
     */
    fun getLayerCount(): Int = mapView.layers.count()

    /**
     * 초기화
     */
    fun clear() {

        CoroutineScope(Dispatchers.Default).launch {

            val job: Deferred<Boolean> = async(Dispatchers.IO) {
                val result = MapLayer.clearLayer(BaseMap, mapView)
                result
            }

            val resultJob = job.await()

            if(resultJob) {
                activity.vm.showLoadingBar(false)
                activity.vm.showSnackbarString("초기화")
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
            activity.vm.showSnackbarString(it.toString())
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
            val geoJSONGeometryWriter = GeoJSONGeometryWriter()

            LogUtil.i("NEW_YN => ${newFeature.properties.getObjectElement("new_yn").string}")
            LogUtil.i("HO_NM =>  ${newFeature.properties.getObjectElement("HO_NM").string}")
            LogUtil.i("GEOMETRY_BOUNDS => ${newFeature.geometry.bounds}")
            LogUtil.i("FEATURE_COUNT => ${featureCollection?.featureCount}")

            LogUtil.i("FEATURE_COLLECTION => ${geoJSONGeometryWriter.writeFeatureCollection(featureCollection)}")
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
                        it.style = MapStyle.setPolygonStyle(
                            MapColor.RED,
                            MapColor.RED,
                            2F
                        )
                        getPropertiesStringValueArr(it)

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

            val isLayerShowToggle = MapEvent.SetLayerReadStatus(activity.binding.switchRead.isChecked).status

            runCatching {
                if (!isLayerShowToggle) throw BaseException("레이어 View 상태값 : False")
            }.fold(
                onSuccess = { activity.vm.showAlertDialog(arr) },
                onFailure = { LogUtil.e(it.toString()) }
            )
        }
    }

    fun getPolygonElementCnt(source: LocalVectorDataSource?): Int {
        var resultCnt = 0
        for (i in 0 until source!!.all.size()) {
            when (source.all[i.toInt()]) {
                is Polygon -> resultCnt++
            }
        }
        return resultCnt
    }


}