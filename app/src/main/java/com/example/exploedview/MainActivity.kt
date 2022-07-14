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
import com.carto.layers.EditableVectorLayer
import com.carto.layers.Layer
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
import com.example.exploedview.map.MapElementColor
import com.example.exploedview.map.MapStyle
import com.example.exploedview.map.listener.MapCustomEventListener
import com.example.exploedview.map.listener.VectorElementSelectEventListener
import com.example.exploedview.util.LogUtil


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), View.OnClickListener {

    // map
    private lateinit var _mapView: MapView
    private lateinit var _mapOpt: Options
    private lateinit var _proj: Projection

    // source
    private var explodeViewSource: LocalVectorDataSource? = null
    private var groupLayerSource: LocalVectorDataSource? = null
    private var floorUpDataSource: LocalVectorDataSource? = null
    private var addLineDataSource: LocalVectorDataSource? = null

    // layer
    private var explodeViewLayer: EditableVectorLayer? = null
    private var groupLayer: EditableVectorLayer? = null
    private var floorUpLayer: EditableVectorLayer? = null
    private var addLineLayer: EditableVectorLayer? = null

    // element arr
    var createPolygonArr = mutableListOf<Polygon>()
    var containsPolygonArr = mutableListOf<Polygon>()
    var clickPosArr = mutableListOf<MapPos>()

    // listener
    var selectListener: VectorElementSelectEventListener? = null

    companion object {
        private const val INCREASE_FLOOR_NUM = 8
        private const val INCREASE_LINE_NUM = 10
    }

    override fun initView() {
        super.initView()

        binding.apply {

            initMapView()

            // MapPos [x=10.226771, y=13.399454, z=0.000000]
            setInitZoomAndPos(22.054665.toFloat(), MapPos(10.226771, 13.399454), 0.5F)
            initExplodeViewLayer()

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

            setLayer()

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

    private fun setLayer() {
        explodeViewSource = LocalVectorDataSource(_proj)
        explodeViewLayer = addLayer("explodeView", explodeViewSource)

        groupLayerSource = LocalVectorDataSource(_proj)
        groupLayer = addLayer("group", groupLayerSource)

        floorUpDataSource = LocalVectorDataSource(_proj)
        floorUpLayer = addLayer("floorUp", floorUpDataSource)

        addLineDataSource = LocalVectorDataSource(_proj)
        addLineLayer = addLayer("addLine", addLineDataSource)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> addElement(EditEventEnum.FLOOR_UP)
            R.id.btn_add_line -> addElement(EditEventEnum.LINE_UP)
            R.id.btn_area -> getContainsElement()
            R.id.btn_reset -> clearElementSource("default")
        }
    }

    /**`´
     * 그룹 지정 레이어 초기화
     */
    private fun clearElementSource(type: String) {

        runCatching {
            when (type) {
                "default", "group" -> true
                else -> throw BaseException("잘못된 이벤트 명입니다.")
            }
        }.onSuccess {

            initExplodeViewLayer()

            clickPosArr.clear()
            groupLayerSource?.clear()
            floorUpDataSource?.clear()
            addLineDataSource?.clear()

            if (MapConst.GROUP) {
                MapStyle.togglePolygonStyle(createPolygonArr, createPolygonArr, type)
            }

        }.onFailure {
            LogUtil.e("[param: $type] ${it}")
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
    private fun initExplodeViewLayer() {

        try {
            explodeViewSource?.clear()
            createPolygonArr.clear()

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
                                MapElementColor.set(ColorEnum.GREEN),
                                MapElementColor.set(ColorEnum.GREEN),
                                2F
                            )
                        )

                        createPolygon.setMetaDataElement("ho", Variant(hoNm))
                        createPolygon.setMetaDataElement("hu", Variant(huNum))
                        createPolygon.setMetaDataElement("cPoedTxt", Variant(cPoedTxt))

                        createPolygonArr.add(createPolygon)
                        elements.add(createPolygon)

                        val minusNum = 1.8
                        val centerPos = MapPos(geometry.getGeometry(j).centerPos.x, geometry.getGeometry(j).centerPos.y + minusNum)
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


            explodeViewSource?.addAll(elements)

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }

    }

    private fun addLayer(name: String, source: LocalVectorDataSource?): EditableVectorLayer {

        val layer: EditableVectorLayer?
        val nameArr = mutableListOf<String>()

        layer = EditableVectorLayer(source)

        runCatching {
            source ?: throw BaseException("레이어 init 에러")
        }.onSuccess {
            setLayerName(layer, "name", Variant(name))
            _mapView.layers.add(layer)
        }.onFailure {
            LogUtil.e(it.toString())
        }.also {
            for (i in 0 until getLayerCount()) {
                nameArr.add(_mapView.layers.get(i).metaData.get("name").string)
            }
        }
        LogUtil.i("생성된 레이어 이름 : ${nameArr}, 현재 생성된 레이어의 갯수 : ${getLayerCount()}")

        return layer
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

        val resultMaxValue: Double

        var south: MapPos
        var west: MapPos
        var north: MapPos
        var east: MapPos

        var addPolygon: Polygon
        var addText: Text
        var vector: MapPosVector

        val element = VectorElementVector()

        try {
            when (type) {

                EditEventEnum.FLOOR_UP -> {

                    _source = floorUpDataSource

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
                                MapElementColor.set(ColorEnum.BLUE),
                                MapElementColor.set(ColorEnum.BLUE),
                                2F
                            )
                        )

                        val convertHo = increaseFloorHoValue(it)

                        addPolygon.setMetaDataElement("ho", Variant(convertHo.toString()))

                        addText = Text(
                            addPolygon.geometry.centerPos,
                            MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                            convertHo.toString()
                        )

                        createPolygonArr.add(addPolygon)

                        element.add(addPolygon)
                        element.add(addText)
                    }

                    _source?.addAll(element)
                }

                EditEventEnum.LINE_UP -> {

                    _source = addLineDataSource

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
                                MapElementColor.set(ColorEnum.HOTPINK),
                                MapElementColor.set(ColorEnum.HOTPINK),
                                2F
                            )
                        )

                        addText = Text(
                            addPolygon.geometry.centerPos,
                            MapStyle.setTextStyle(MapElementColor.set(ColorEnum.BLACK), 20F),
                            "라인 추가"
                        )

                        createPolygonArr.add(addPolygon)

                        element.add(addPolygon)
                        element.add(addText)
                    }

                    _source?.addAll(element)
                }

            }

        } catch (e: BaseException) {
            LogUtil.e(e.toString())
        }

    }

    private fun increaseFloorHoValue(it: Polygon): Int {
        val convertHo = when (it.getMetaDataElement("ho").string.length) {
            3 -> {
                it.getMetaDataElement("ho").string.toInt() + 100
            }
            else -> {
                it.getMetaDataElement("ho").string.toInt() + 1000
            }
        }
        return convertHo
    }

    /**
     * 그룹 영역 내 Element 가져오기
     */
    fun getContainsElement() {

        val bool = containsPolygonArr.isNotEmpty()

        runCatching {
            if (!bool) throw BaseException("그룹영역이 지정되지 않았습니다. \n 그룹영역을 드래그해주세요.")
        }.onSuccess {
            //성공시만 실행
            clickPosArr.clear()
            groupLayerSource?.clear()

            MapStyle.togglePolygonStyle(createPolygonArr, containsPolygonArr, "group")

            MapConst.GROUP = true

        }.onFailure { it: Throwable ->
            //실패시만 실행 (try - catch문의 catch와 유사)
            LogUtil.e("group status: $bool, ${it}")
            showToast(it.message)
        }
    }
}