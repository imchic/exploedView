package com.example.exploedview

import android.view.View
import android.widget.Button
import com.carto.components.Options
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.MapRange
import com.carto.core.Variant
import com.carto.datasources.LocalVectorDataSource
import com.carto.geometry.FeatureCollection
import com.carto.geometry.GeoJSONGeometryReader
import com.carto.geometry.MultiPolygonGeometry
import com.carto.graphics.Color
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
import com.carto.layers.VectorLayer
import com.carto.projections.Projection
import com.carto.ui.MapView
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.carto.vectorelements.VectorElementVector
import com.example.exploedview.databinding.ActivityMainBinding
import com.example.exploedview.enums.EditEventEnum
import com.example.exploedview.extension.Extensions.max
import com.example.exploedview.listener.EditEventListener
import com.example.exploedview.listener.MapCustomEventListener


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), View.OnClickListener {

    val utils: Utils by lazy { Utils(localClassName) }

    private val _resetButton: Button by lazy { binding.btnReset }
    private val _selectButton: Button by lazy { binding.btnSelect }
    private val _groupButton: Button by lazy { binding.btnGroup }
    val _areaButton: Button by lazy { binding.btnArea }

    private lateinit var _mapView: MapView

    private lateinit var _mapOpt: Options
    private lateinit var _proj: Projection

    // source
    private var _groupLayerSource: LocalVectorDataSource? = null
    private var _addFloorDataSource: LocalVectorDataSource? = null
    private var _addLineDataSource: LocalVectorDataSource? = null
    private var _groupLocalVectorDataSource: LocalVectorDataSource? = null

    // layer
    private var _groupVecotrLayer: EditableVectorLayer? = null
    private var _copyVecotrLayer: VectorLayer? = null
    private var _vectorLayer: EditableVectorLayer? = null

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var clickPosArr = mutableListOf<MapPos>()

    // listener
    private var _mapCustomEventListener: MapCustomEventListener? = null
    private var _editEventListener: EditEventListener? = null
//    private var _selectListener: VectorElementSelectEventListener? = null
//    private var _deselectListener: VectorElementDeselectListener? = null

    // bool
//    var _selectFlag: Boolean = false
//    var _groupFlag: Boolean = false
//
//    private var _addFloorFlag: Boolean = false
//    private var _addLineFlag: Boolean = false

    companion object {
        private const val INCREASE_FLOOR_NUM = 8
        private const val INCREASE_LINE_NUM = 10
        private const val FILL_ALPHA: Short = 50
    }


    override fun initView() {
        super.initView()

        binding.apply {

            initMapView()
            setInitZoomAndPos(21F, MapPos(10.0001, 7.5), 0.5F)
            createGeoJsonLayer()

            btnAddFloor.setOnClickListener(this@MainActivity)
            btnAddLine.setOnClickListener(this@MainActivity)
        }
    }

    private fun initMapView() {

        try {
            _mapView = binding.cartoMapView
            _mapOpt = _mapView.options
            _proj = _mapOpt.baseProjection

            _groupLayerSource = LocalVectorDataSource(_proj)
            _groupVecotrLayer = EditableVectorLayer(_groupLayerSource)

            _mapView.mapEventListener =
                MapCustomEventListener(this@MainActivity, _mapView, _groupLayerSource, _groupVecotrLayer, clickPosArr)
        } catch (e: Exception) {
            utils.logE(e.toString())
        }

        // 맵 옵션
        _mapOpt.apply {
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
        }

//        _localVectorDataSource = LocalVectorDataSource(_proj)

        /**
         * 선택 Event
         */
        _selectButton.setOnClickListener {
//            _selectFlag = !_selectFlag
//
//            if (_selectFlag) {
//                getToast("선택모드")
//            } else {
//                getToast("비선택모드")
//                setDefaultLayerStyle()
//            }
//
//            utils.logI(_selectFlag.toString())
        }

        /**
         * 그룹 바운더리 영역 삭제 (초기화)
         */
        _resetButton.setOnClickListener {
            removeLayer()
        }

        /**
         * 그룹영역 내 선택된 객체 가져오기
         */
        _areaButton.setOnClickListener {

            try {
                createPolygonArr.map { poly ->
                    val polyContainsFlag: Boolean? = _editEventListener?.withinPolygonArr?.contains(poly)

                    if (polyContainsFlag == true) {
                        poly.style = MapStyle.setPolygonStyle(Color(0, 255, 0, FILL_ALPHA), Color(0, 255, 0, 255), 2F)
                    }
                }

                _groupLocalVectorDataSource?.clear()


            } catch (e: Exception) {
                utils.logE(e.toString())
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> addExplodeVectorElement(EditEventEnum.FLOOR_UP)
            R.id.btn_add_line -> addExplodeVectorElement(EditEventEnum.LINE_UP)
        }
    }

    private fun setDefaultLayerStyle() {
        createPolygonArr.map { data ->
            data.style = MapStyle.setPolygonStyle(Color(255, 255, 0, FILL_ALPHA), Color(0, 0, 0, 255), 2F)
        }
    }

    /**`´
     * 그룹 지정 레이어 초기화
     */
    private fun removeLayer() {

        try {
            clickPosArr.clear()
            _groupLayerSource?.clear()

            utils.logI("layer count => ${getLayerCount()}")

            for (i in 0 until getLayerCount()) {
                utils.logI(_mapView.layers.get(i).metaData.get("layerName").string)
            }

            getToast("초기화를 헀습니다.")

        } catch (e: Exception) {
            utils.logE(e.toString())
        }

    }

    /**
     * 레이어의 갯수
     * @return Int
     */
    fun getLayerCount(): Int = _mapView.layers.count()

    /**
     * 전개도 레이어 표출
     */
    private fun createGeoJsonLayer() {

        try {

            val source = LocalVectorDataSource(_proj)
            val layer = VectorLayer(source)

            setLayerName(layer, "layerName", Variant("explodeViewLayer"))

            _mapView.layers.add(layer)

            val elements = VectorElementVector()

            val features = getGeoJsonFeature("test.geojson")
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
                            MapStyle.setPolygonStyle(Colors.setFillColor("green", FILL_ALPHA), Color(0, 0, 0, 255), 2F)
                        )
                        createPolygonArr.add(createPolygon)

                        elements.add(createPolygon)

                        val minusNum = 1.8
                        val centerPos =
                            MapPos(geometry.getGeometry(j).centerPos.x, geometry.getGeometry(j).centerPos.y + minusNum)
                        val middlePos = MapPos(centerPos.x, centerPos.y - minusNum)
                        val botPos = MapPos(middlePos.x, middlePos.y - minusNum)

                        elements.add(Text(centerPos, MapStyle.setTextStyle(Color(0, 0, 0, 255), 30F), hoNm))
                        elements.add(Text(middlePos, MapStyle.setTextStyle(Color(255, 0, 0, 255), 32F), huNum))
                        elements.add(Text(botPos, MapStyle.setTextStyle(Color(0, 0, 0, 255), 30F), cPoedTxt))
                    }

                }


            }


            source.addAll(elements)

        } catch (e: Exception) {
            utils.logI(e.toString())
        }

    }

    /**
     * 레이어 명
     * @param layer VectorLayer
     * @param key String
     * @param name Variant
     */
    fun setLayerName(layer: Layer?, key: String, name: Variant) = layer?.setMetaDataElement(key, name)

    /**
     * GeoJson 값 가져오기
     * @param fileName  Asset Package 내 불러올 파일명 (json type)
     * @return FeatureCollection?
     */
    private fun getGeoJsonFeature(fileName: String): FeatureCollection? {
        val stream = assets.open(fileName)
        val size = stream.available()
        val buffer = ByteArray(size)
        stream.read(buffer)
        stream.close()
        val json = String(buffer, charset("UTF-8"))
        utils.logI("json string => $json")
        val reader = GeoJSONGeometryReader()
//        reader.targetProjection = _proj
        return reader.readFeatureCollection(json)
    }

    /**
     * 최초 줌 지정 및 위치 지정
     * @param zoom Float
     * @param pos MapPos
     * @param duration Float
     */
    private fun setInitZoomAndPos(zoom: Float, pos: MapPos, duration: Float) {
        _mapView.setZoom(zoom, duration)
        _mapView.setFocusPos(pos, duration)
    }


    /**
     * 층 추가, 라인 추가 이벤트
     * @param type EditEventEnum ( 층 추가, 라인 추가 )
     */
    fun addExplodeVectorElement(type: EditEventEnum) {

        val _source: LocalVectorDataSource?
        val _maxValArr = arrayListOf<Int>()
        val _filterArr: MutableList<Polygon>

        _source = LocalVectorDataSource(_proj)

        val resultMaxValue: Double

        var south: MapPos
        var west: MapPos
        var north: MapPos
        var east: MapPos

        var addPolygon: Polygon
        var vector: MapPosVector

        when (type) {

            EditEventEnum.FLOOR_UP -> {
                createPolygonArr.map { _maxValArr.add(it.bounds.max.y.toInt()) /* 최대값 구하기*/ }

                val max: Int = _maxValArr.max(_maxValArr)
                resultMaxValue = max.toDouble()

                _filterArr = createPolygonArr.filter { it.bounds.max.y == resultMaxValue } as MutableList<Polygon>

                _filterArr.map {

                    south = MapPos(it.bounds.min.x, it.bounds.max.y)
                    west = MapPos(it.bounds.max.x, it.bounds.max.y)

                    north = MapPos(it.bounds.max.x, it.bounds.max.y + INCREASE_FLOOR_NUM)
                    east = MapPos(it.bounds.min.x, it.bounds.max.y + INCREASE_FLOOR_NUM)

                    vector = MapPosVector()
                    vector.apply { add(south); add(west); add(north); add(east) }

                    addPolygon = Polygon(
                        vector,
                        MapStyle.setPolygonStyle(Color(1, 113, 95, FILL_ALPHA), Color(1, 113, 95, 255), 2F)
                    )

                    createPolygonArr.add(addPolygon)
                    _source.add(addPolygon)

                }
            }

            EditEventEnum.LINE_UP -> {
                createPolygonArr.map { _maxValArr.add(it.bounds.max.x.toInt()) /* 최대값 구하기*/ }
                val max: Int = _maxValArr.max(_maxValArr)
                resultMaxValue = max.toDouble()

                _filterArr =
                    createPolygonArr.filter { it.bounds.max.x.toInt() == resultMaxValue.toInt() } as MutableList<Polygon>

                _filterArr.map {

                    south = MapPos(it.bounds.max.x, it.bounds.min.y)
                    west = MapPos(it.bounds.max.x + INCREASE_LINE_NUM, it.bounds.min.y)

                    north = MapPos(it.bounds.max.x + INCREASE_LINE_NUM, it.bounds.max.y)
                    east = MapPos(it.bounds.max.x, it.bounds.max.y)

                    vector = MapPosVector()
                    vector.apply { add(south); add(west); add(north); add(east) }

                    addPolygon = Polygon(
                        vector,
                        MapStyle.setPolygonStyle(Color(255, 0, 221, FILL_ALPHA), Color(255, 0, 221, 255), 2F)
                    )

                    createPolygonArr.add(addPolygon)
                    _source.add(addPolygon)

                }
            }

        }

        _copyVecotrLayer = VectorLayer(_source)
        _mapView.layers.add(_copyVecotrLayer)

    }
}