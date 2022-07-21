package com.example.exploedview.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.util.LogUtil

@SuppressLint("StaticFieldLeak")
object BaseMap: MapViewModel() {

    private lateinit var context: Context
    private lateinit var activity: MapActivity
    lateinit var mapViewModel: MapViewModel

    // map
    private lateinit var mapView: MapView
    private lateinit var mapOpt: Options
    lateinit var proj: Projection

    // source
    private var explodedViewSource: LocalVectorDataSource? = null
    var groupLayerSource: LocalVectorDataSource? = null
    var floorUpDataSource: LocalVectorDataSource? = null
    var addLineDataSource: LocalVectorDataSource? = null
    var addHoDataSource: LocalVectorDataSource? = null

    // layer
    private var explodedViewLayer: EditableVectorLayer? = null
    private var groupLayer: EditableVectorLayer? = null
    private var floorUpLayer: EditableVectorLayer? = null
    private var addLineLayer: EditableVectorLayer? = null
    private var addHoLayer: EditableVectorLayer? = null

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var containsPolygonArr = mutableListOf<Polygon>()
    var selectPolygonArr = mutableListOf<Polygon>()

    var clickPosArr = mutableListOf<MapPos>()

    // listener
    var selectListener: VectorElementSelectEventListener? = null

    // feature
    var featureCollection: FeatureCollection? = null


    /**
     * 최초 맵 세팅
     * @param mapView MapView
     * @param activity Activity
     * @param context Context
     * @param viewModel MapViewModel
     */
    fun init(mapView: MapView, activity: Activity, context: Context, viewModel: MapViewModel) {

        this.context = context
        this.activity = activity as MapActivity
        mapViewModel = viewModel

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
        groupLayerSource = LocalVectorDataSource(proj)
        floorUpDataSource = LocalVectorDataSource(proj)
        addLineDataSource = LocalVectorDataSource(proj)
        addHoDataSource = LocalVectorDataSource(proj)

        val layerArr = mutableListOf(explodedViewLayer, groupLayer, floorUpLayer, addLineLayer, addHoLayer)
        setLayer(layerArr)

        MapLayer.explodedView(this.activity, explodedViewSource, createPolygonArr)
        this.mapView.mapEventListener = MapCustomEventListener(this.mapView, groupLayerSource, clickPosArr)

//        MapStyle.createGeoJSONLayer(_activity, _mapView)
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
        val assetManager = context.resources.assets
        val stream = assetManager.open(fileName)
        val size = stream.available()
        val buffer = ByteArray(size)
        stream.read(buffer)
        stream.close()
        val json = String(buffer, charset("UTF-8"))
        LogUtil.i("response data => $json")
        val reader = GeoJSONGeometryReader()
//        reader.targetProjection = _proj
        return reader.readFeatureCollection(json)
    }

    /**
     * 레이어 세팅
     */
    private fun setLayer(layers: MutableList<EditableVectorLayer?>) {

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
                    source = groupLayerSource
                    layers[index] = EditableVectorLayer(source)
                }
                MapLayerName.ADD_FLOOR -> {
                    source = floorUpDataSource
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
                else -> throw BaseException("잘못된 레이어 명입니다.")
            }

            setLayerName(layers[index], "name", Variant(name.value))
            mapView.layers.add(layers[index])

        }
            .also {
                for (i in 0 until getLayerCount()) {
                    nameArr.add(mapView.layers.get(i).metaData.get("name").string)
                }
                LogUtil.i("생성된 레이어 이름 : ${nameArr}, 현재 생성된 레이어의 갯수 : ${getLayerCount()}")
                activity._viewModel.getBaseLayers.value = getLayerCount().toString()
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

        clickPosArr.clear()

        groupLayerSource?.clear()
        floorUpDataSource?.clear()
        addLineDataSource?.clear()

        MapLayer.floorElement.clear()
        MapLayer.lineElement.clear()

        explodedViewSource?.clear()
        MapLayer.explodedView(activity, explodedViewSource, createPolygonArr)

        selectPolygonArr.clear()
        containsPolygonArr.clear()

        mapViewModel.apply {
            getTotalExplodedPolygon.value = createPolygonArr.size.toString()
            getBaseLayers.value = mapView.layers.count().toString()
            getSelectExplodedPolygon.value = selectPolygonArr.size.toString()
            getGroupExplodedPolygon.value = containsPolygonArr.size.toString()
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
            groupLayerSource?.clear()

            group(parents, child)

        }.onFailure { it: Throwable ->
            LogUtil.e("group status: $bool, $it")
        }
    }

    /**
     * Feature 추가
     * @param it Polygon
     * @param value Int
     */
    fun addFeatures(geom: Geometry?, value: Int) {

        runCatching {
            geom ?: throw BaseException("Feature 타입에 필요한 지오메트리가 존재하지 않습니다.")
        }.onSuccess {
            val featureBuilder = FeatureBuilder()
            featureBuilder.geometry = geom as PolygonGeometry
            featureBuilder.setPropertyValue("new_yn", Variant("Y"))
            featureBuilder.setPropertyValue("HO_NM", Variant(value.toString()))
            val newFeature = featureBuilder.buildFeature()

            LogUtil.d(newFeature.properties.getObjectElement("new_yn").string)
            LogUtil.d(newFeature.properties.getObjectElement("HO_NM").string)
            LogUtil.d(newFeature.geometry.bounds.toString())

            val featureVector = FeatureVector()
            featureVector.add(newFeature)

            featureCollection = FeatureCollection(featureVector)
            LogUtil.d("featureCount => ${featureCollection?.featureCount}")

        }.onFailure {
            LogUtil.e(it.toString())

        }.also {
            val geoJSONGeometryWriter = GeoJSONGeometryWriter()
            LogUtil.d(geoJSONGeometryWriter.writeFeatureCollection(featureCollection))
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
                            MapElementColor.set(ColorEnum.YELLOW),
                            MapElementColor.set(ColorEnum.YELLOW),
                            2F
                        )

                        getPropertiesStringValueArr(it)

                        it.setMetaDataElement("SELECT", Variant("y"))
                        selectPolygonArr.add(it)
                    }
                    "y" -> {
                        it.style = MapStyle.setPolygonStyle(
                            MapElementColor.set(ColorEnum.TEAL),
                            MapElementColor.set(ColorEnum.TEAL),
                            2F
                        )
                        it.setMetaDataElement("SELECT", Variant("n"))
                        selectPolygonArr.remove(it)
                    }
                    else -> {
                        throw BaseException("잘못된 select Event 발생")
                    }
                }

            }
            .also {
                activity.runOnUiThread {
                    activity._viewModel.getSelectExplodedPolygon.value = selectPolygonArr.size.toString()
                }
            }

        LogUtil.d("선택된 전개도 폴리곤의 개수 : ${selectPolygonArr.size}")
    }

    /**
     * 부모 영역 내 포함된 그룹영역
     * @param parnnts MutableList<Polygon>
     * @param child MutableList<Polygon>
     */
    fun group(parnnts: MutableList<Polygon>, child: MutableList<Polygon>) {
        parnnts
            .filter { child.contains(it) }
            .map {
                it.style = MapStyle.setPolygonStyle(
                    MapElementColor.set(ColorEnum.PURPLE),
                    MapElementColor.set(ColorEnum.PURPLE),
                    2F
                )
            }
    }

    /**
     * 폴리곤 내 프로퍼티 값 세팅하기
     * @param properties Variant
     * @param value String
     * @return String
     */
    fun setPropertiesStringValue(properties: Variant, arr: ArrayList<String>, polygon: Polygon) {
        arr.map {
            val getValue = properties.getObjectElement(it).string
            polygon.setMetaDataElement(it, Variant(getValue))
        }
    }

    fun getPropertiesStringValue(polygon: Polygon, value: String): String{
        return polygon.getMetaDataElement(value).string
    }

    fun getPropertiesStringValueArr(polygon: Polygon){
        var result = ""
        for((key, value) in MapConst.PROPERTIES_VALUE_MAP){
            result += "$key : ${polygon.metaData.get(value).string} \n"
        }
        val arr  = ArrayList<String>()
        arr.add("레이어 정보")
        arr.add(result)
        activity.runOnUiThread {
            if(activity._viewModel.getLayerReadStatus.value == true){
                mapViewModel.showAlertDialog(arr)
            } else {
                val toggleValue = activity._viewModel.getLayerReadStatus.value ?: false
                activity._viewModel.showSnackbar("레이어 정보 열람 $toggleValue")
            }
        }
    }


}