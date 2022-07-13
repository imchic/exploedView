package com.example.exploedview

import android.view.View
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
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.databinding.ActivityMainBinding
import com.example.exploedview.enums.ColorEnum
import com.example.exploedview.enums.EditEventEnum
import com.example.exploedview.extension.Extensions.max
import com.example.exploedview.map.listener.VectorElementEditEventListener
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementDeselectListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.map.MapStyle
import com.example.exploedview.util.LogUtil


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), View.OnClickListener {

    // map
    private lateinit var _mapView: MapView
    private lateinit var _mapOpt: Options
    private lateinit var _proj: Projection

    // source
    private var groupLayerSource: LocalVectorDataSource? = null
    private var _addFloorDataSource: LocalVectorDataSource? = null
    private var _addLineDataSource: LocalVectorDataSource? = null
    private var _groupLocalVectorDataSource: LocalVectorDataSource? = null

    // layer
    private var groupLayer: EditableVectorLayer? = null
    private var _copyVecotrLayer: VectorLayer? = null
    private var _vectorLayer: EditableVectorLayer? = null

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var containsPolygonArr = mutableListOf<Polygon>()
    var clickPosArr = mutableListOf<MapPos>()

    // listener
    private var _mapCustomEventListener: MapCustomEventListener? = null

    var editEventListener: VectorElementEditEventListener? = null
    var selectListener: VectorElementSelectEventListener? = null
    var deselectListener: VectorElementDeselectListener? = null

    companion object {
        private const val INCREASE_FLOOR_NUM = 8
        private const val INCREASE_LINE_NUM = 10
    }

    override fun initView() {
        super.initView()

        binding.apply {

            initMapView()
            setInitZoomAndPos(21F, MapPos(10.0001, 7.5), 0.5F)
            createGeoJsonLayer()

            this@MainActivity.run {
                btnAddFloor.setOnClickListener(this)
                btnAddLine.setOnClickListener(this)
                btnArea.setOnClickListener(this)
                btnReset.setOnClickListener(this)
            }

        }
    }

    private fun initMapView() {

        try {
            _mapView = binding.cartoMapView
            _mapOpt = _mapView.options
            _proj = _mapOpt.baseProjection

            groupLayerSource = LocalVectorDataSource(_proj)
            groupLayer = EditableVectorLayer(groupLayerSource)

            _mapView.mapEventListener =
                MapCustomEventListener(this@MainActivity, _mapView, groupLayerSource, groupLayer, clickPosArr)

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }

        // 맵 옵션
        _mapOpt.apply {
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> addElement(EditEventEnum.FLOOR_UP)
            R.id.btn_add_line -> addElement(EditEventEnum.LINE_UP)
            R.id.btn_area -> getContainsElement()
            R.id.btn_reset -> clearElementSource()
        }
    }

    /**`´
     * 그룹 지정 레이어 초기화
     */
    private fun clearElementSource() {
        runCatching {
            clickPosArr.clear()
            groupLayerSource?.clear()
        }.onSuccess {
            LogUtil.i("초기화 성공")
        }.onFailure {
            LogUtil.e(it.toString())
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
                            MapStyle.setPolygonStyle(
                                MapElementColor.set(ColorEnum.GREEN, MapConst.FILL_OPACITY),
                                Color(0, 0, 0, 255),
                                2F
                            )
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

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
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
        LogUtil.i("json string => $json")
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
    fun addElement(type: EditEventEnum) {

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

        try {
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
                            MapStyle.setPolygonStyle(
                                Color(1, 113, 95, MapConst.FILL_OPACITY),
                                Color(1, 113, 95, 255),
                                2F
                            )
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
                            MapStyle.setPolygonStyle(
                                Color(255, 0, 221, MapConst.FILL_OPACITY),
                                Color(255, 0, 221, 255),
                                2F
                            )
                        )

                        createPolygonArr.add(addPolygon)
                        _source.add(addPolygon)

                    }
                }

                else -> throw BaseException("지정되지 않은 이벤트 입니다.")

            }

            _copyVecotrLayer = VectorLayer(_source)
            _mapView.layers.add(_copyVecotrLayer)

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }

    }

    /**
     * 그룹 영역 내 Element 가져오기
     */
    fun getContainsElement() {

        runCatching {
            createPolygonArr.map { poly ->

                when {
                    containsPolygonArr.isNotEmpty() -> {
                        val polyContainsFlag: Boolean? = containsPolygonArr?.contains(poly)

                        if (polyContainsFlag == true) {
                            poly.style = MapStyle.setPolygonStyle(
                                MapElementColor.set(ColorEnum.PURPLE, MapConst.FILL_OPACITY),
                                MapElementColor.set(ColorEnum.PURPLE, 255),
                                2F
                            )
                        }
                    }
                    else -> throw BaseException("그룹영역을 지정해주세요.")
                }
            }
            clearElementSource()

        }.onFailure {
            when (it) {
                is BaseException -> {
                    LogUtil.e(it.toString())
                }
            }
        }

    }
}