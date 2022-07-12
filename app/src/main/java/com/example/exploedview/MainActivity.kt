package com.example.exploedview

import android.view.View
import android.widget.Button
import com.carto.components.Options
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.MapRange
import com.carto.datasources.LocalVectorDataSource
import com.carto.graphics.Color
import com.carto.layers.EditableVectorLayer
import com.carto.layers.VectorLayer
import com.carto.projections.Projection
import com.carto.styles.*
import com.carto.ui.MapView
import com.carto.vectorelements.Line
import com.carto.vectorelements.Point
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.example.exploedview.databinding.ActivityMainBinding
import com.example.exploedview.enums.EditEventEnum
import com.example.exploedview.listener.EditEventListener
import com.example.exploedview.listener.MapCustomEventListener
import com.example.exploedview.listener.VectorElementDeselectListener
import com.example.exploedview.listener.VectorElementSelectEventListener
import java.util.*


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), View.OnClickListener {

    val utils: Utils by lazy { Utils(localClassName) }

    private val _resetButton: Button by lazy { binding.btnReset }
    private val _selectButton: Button by lazy { binding.btnSelect }
    private val _groupButton: Button by lazy { binding.btnGroup }

    val _areaButton: Button by lazy { binding.btnArea }

    private val _alpha: Short by lazy { 50 }

    private lateinit var _mapView: MapView

    private lateinit var _mapOpt: Options
    private lateinit var _proj: Projection

    private var _localVectorDataSource: LocalVectorDataSource? = null

    private var _addFloorDataSource: LocalVectorDataSource? = null
    private var _addLineDataSource: LocalVectorDataSource? = null

    private var _groupLocalVectorDataSource: LocalVectorDataSource? = null
    private var _posVector: MapPosVector? = null

    private var _pointStyleBuilder: PointStyleBuilder? = null
    private var _lineStyleBuilder: LineStyleBuilder? = null
    private var _polygonStyleBuilder: PolygonStyleBuilder? = null
    private var _textStyleBuilder: TextStyleBuilder? = null

    private var _vecotrLayer: EditableVectorLayer? = null
    private var _groupVecotrLayer: EditableVectorLayer? = null

    private var _copyVecotrLayer: VectorLayer? = null
    private var _codeArr = mutableListOf<String>()
    private var _pointGroupVertaxArr = mutableListOf<MapPos>()
    private var _posVectorArr = mutableListOf<MapPosVector>()

    private var _labelHoNmArr = mutableListOf<String>()
    private var _labelHuNumArr = mutableListOf<String>()
    private var _labelCpoedTxtArr = mutableListOf<String>()
    var makePolygonArr = mutableListOf<Polygon>()

    private var _mapCustomEventListener: MapCustomEventListener? = null
    private var _editEventListener: EditEventListener? = null
    private var _selectListener: VectorElementSelectEventListener? = null
    private var _deselectListener: VectorElementDeselectListener? = null

    private var _increaseFloorNum: Int = 8
    private var _increaseLineNum: Int = 10

    var _selectFlag: Boolean = false
    var _groupFlag: Boolean = false

    private var _addFloorFlag: Boolean = false
    private var _addLineFlag: Boolean = false

    override fun initView() {
        super.initView()

        binding.apply {
            initMapView()
            setInitZoomAndPos(22F, MapPos(10.0001, 7.5), 0.5F)
            drawExploedLayer()

            btnAddFloor.setOnClickListener(this@MainActivity)
            btnAddLine.setOnClickListener(this@MainActivity)
        }
    }

    private fun initMapView() {
        _mapView = binding.cartoMapView
        _mapOpt = _mapView.options
        _proj = _mapOpt.baseProjection
        _mapCustomEventListener = MapCustomEventListener(this@MainActivity)


//        val overlayBitmap = BitmapUtils.loadBitmapFromAssets("ci.png")


        // 맵 옵션
        _mapOpt.apply {
            tiltRange = MapRange(90f, 90f) // 틸트 고정
            isRotatable = false // 회전
            isZoomGestures = false
//            backgroundBitmap = overlayBitmap
            watermarkAlignmentX = -2.0f
            watermarkAlignmentY = -2.0f
            watermarkScale = 0.01f
        }

        utils.logI("watermark xy => ${_mapOpt.watermarkAlignmentX}, ${_mapOpt.watermarkAlignmentY}")

        _localVectorDataSource = LocalVectorDataSource(_proj)

        /**
         * 그룹 바운더리 영역 생성
         */
        _groupButton.setOnClickListener {

            _groupLocalVectorDataSource?.clear()

            if (_pointGroupVertaxArr.isNotEmpty()) {
                when (_pointGroupVertaxArr.size) {

                    // 포인트
                    1 -> {
                        for (pos in _pointGroupVertaxArr) {
                            val pointGroupSymbol = Point(pos, setPointStyle(Color(0, 0, 255, 255), 13F))
                            _groupLocalVectorDataSource?.add(pointGroupSymbol)
                        }
                    }

                    // 라인
                    2 -> {
                        val groupLineMapPosVector = MapPosVector()
                        for (pos in _pointGroupVertaxArr) {
                            groupLineMapPosVector.add(pos)
                        }
                        val lineGroupSymbol = Line(
                            groupLineMapPosVector,
                            setLineStyle(Color(0, 0, 255, 255), LineJoinType.LINE_JOIN_TYPE_BEVEL, 8F)
                        )
//                    lineGroupSymbol.setMetaDataElement("ClickText", Variant("Line nr 1"))
                        _groupLocalVectorDataSource?.add(lineGroupSymbol)
                    }

                    // 폴리곤
                    else -> {
                        val groupPolygonMapPosVector = MapPosVector()
                        for (pos in _pointGroupVertaxArr) {
                            groupPolygonMapPosVector.add(pos)
                        }
                        val polygonGroupSymbol = Polygon(
                            groupPolygonMapPosVector,
                            setPolygonStyle(Color(0, 0, 255, _alpha), Color(0, 0, 255, 255), 2F)
                        )
                        _groupLocalVectorDataSource?.add(polygonGroupSymbol)
                    }

                }

                _groupVecotrLayer = EditableVectorLayer(_groupLocalVectorDataSource)
                _selectListener = VectorElementSelectEventListener(this@MainActivity, _groupVecotrLayer)

                _groupVecotrLayer?.run {
                    vectorEditEventListener = _editEventListener
                    vectorElementEventListener = _selectListener
                }

                _mapView.layers?.add(_groupVecotrLayer)

                it.isEnabled = false

                //selectToggle = true
                _groupFlag = true
                _selectButton.isEnabled = false

            } else {
                getToast("화면을 클릭하여 그룹 영역을 지정해주세요 \n (포인트 3개이상 필요)")
            }


        }

        /**
         * 선택 Event
         */
        _selectButton.setOnClickListener {
            _selectFlag = !_selectFlag

            if (_selectFlag) {
                getToast("선택모드")
            } else {
                getToast("비선택모드")
                setDefaultLayerStyle()
            }

            utils.logI(_selectFlag.toString())
        }

        /**
         * 그룹 바운더리 영역 삭제 (초기화)
         */
        _resetButton.setOnClickListener {

            removeLayer()
            drawExploedLayer()

            _selectFlag = false
            _addFloorFlag = false
            _addLineFlag = false

            getToast("초기화를 헀습니다.")
        }

        /**
         * 그룹영역 내 선택된 객체 가져오기
         */
        _areaButton.setOnClickListener {

            try {
                makePolygonArr.map { poly ->

                    val polyContainsFlag: Boolean? = _editEventListener?.withinPolygonArr?.contains(poly)

                    if (polyContainsFlag == true) {
                        poly.style = setPolygonStyle(Color(0, 255, 0, _alpha), Color(0, 255, 0, 255), 2F)
                    }
                }

                _groupLocalVectorDataSource?.clear()


                //lastRemoveMapViewLayer()

//                for(i in 0 .. getLayerCount()){
//                    utils.logI("${_mapView.layers} [$i]")
//                }

//                utils.logI(_mapView.layers.toString())

//                _mapView.layers.delete()


            } catch (e: Exception) {
                utils.logE(e.toString())
            }

//            removeLayer()

        }
    }


    private fun lastRemoveMapViewLayer() {
        _mapView.layers.remove(_mapView.layers.get(getLayerCount() - 1))
    }

    private fun setDefaultLayerStyle() {
        makePolygonArr.map { data -> data.style = setPolygonStyle(Color(255, 255, 0, _alpha), Color(0, 0, 0, 255), 2F) }
    }

    /**`´
     * 그룹 지정 레이어 초기화
     */
    private fun removeLayer() {

        try {

            _increaseFloorNum = 8
            _increaseLineNum = 10

            _mapView.layers.removeAll(_mapView.layers.all)

            makePolygonArr.clear()
            _codeArr.clear()
            _labelHoNmArr.clear()
            _labelHuNumArr.clear()
            _labelCpoedTxtArr.clear()

            _mapCustomEventListener?.groupMapPosArr?.clear()
            _groupLocalVectorDataSource?.clear()
            _localVectorDataSource?.clear()
            _addFloorDataSource?.clear()
            _addLineDataSource?.clear()

            utils.logI("before layer count [${getLayerCount()}]")
            utils.logI("after layer count [${getLayerCount()}]")

            _groupButton.isEnabled = false
            _areaButton.isEnabled = false
            _selectButton.isEnabled = true

        } catch (e: Exception) {
            utils.logE(e.toString())
        }

    }

    /**
     * 레이어의 갯수
     * @return Int
     */
    private fun getLayerCount(): Int = _mapView.layers.count()

    /**
     * 전개도 레이어 표출
     */
    private fun drawExploedLayer() {

        try {
            val tempDataStr =
                "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,7.5],[1.1826975241075102E-4,7.5],[1.1826975241075102E-4,15],[10.0001,15],[10.0001,7.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"201\",\"hhd_sum\":0,\"seq_no\":1,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0,\"coord_x\":5,\"coord_y\":11,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"39\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.111015462\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,0.0],[1.1826975241075102E-4,0.0],[1.1826975241075102E-4,7.5],[10.0001,7.5],[10.0001,0.0]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"101\",\"hhd_sum\":0,\"seq_no\":2,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0,\"coord_x\":5,\"coord_y\":4,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"37\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.111015461\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,22.5],[11.0001,22.5],[11.0001,30],[21.0001,30],[21.0001,22.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"402\",\"hhd_sum\":0E-15,\"seq_no\":4.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":26.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"44\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224349\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[11.0001,0.0],[11.0001,7.5],[21.0001,7.5],[21.0001,0.0],[11.0001,0.0]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"102\",\"hhd_sum\":0E-15,\"seq_no\":3.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":4.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"38\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224348\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,7.5],[11.0001,7.5],[11.0001,15],[21.0001,15],[21.0001,7.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"202\",\"hhd_sum\":0E-15,\"seq_no\":9.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":11.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"40\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224347\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,15],[11.0001,15],[11.0001,22.5],[21.0001,22.5],[21.0001,15]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"302\",\"hhd_sum\":0E-15,\"seq_no\":8.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":19.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"42\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224346\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,22.5],[1.1826975241075102E-4,22.5],[1.1826975241075102E-4,30],[10.0001,30],[10.0001,22.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"401\",\"hhd_sum\":0E-15,\"seq_no\":5.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":5.000000000000000,\"coord_y\":26.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"43\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224344\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,15],[1.1826975241075102E-4,15],[1.1826975241075102E-4,22.5],[10.0001,22.5],[10.0001,15]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"301\",\"hhd_sum\":0E-15,\"seq_no\":7.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":5.000000000000000,\"coord_y\":19.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"41\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224345\"}]}"
            val parseJson = httpResultToJsonObject(tempDataStr)

            val getFeatures = parseJson.get("features").asJsonArray
            for (i in 0 until getFeatures.size()) {

                _codeArr.add(
                    getFeatures.get(i).asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray.get(
                        0
                    ).asJsonArray.get(0).asJsonArray.toString()
                )

                val properties = getFeatures.get(i).asJsonObject.get("properties").asJsonObject
                val hoNm: String = properties.get("ho_nm").asString
                val huNum: String = properties.get("hu_num").asString
                val cPoedTxt: String = properties.get("c_poed_txt").asString

                _labelHoNmArr.add(hoNm)
                _labelHuNumArr.add(huNum)
                _labelCpoedTxtArr.add(cPoedTxt)

            }

        } catch (e: Exception) {
            utils.logI(e.toString())
        }

        _posVectorArr = mutableListOf()

        _codeArr.forEach { obj ->

            _posVector = MapPosVector()

            obj.split("],").forEach { data ->

                val geom = data.replace("[", "").replace("]", "")
                var x: Double
                var y: Double

                geom.run {
                    x = split(",")[0].replace("[", "").toDouble()
                    y = split(",")[1].replace("[", "").toDouble()
                }
                _posVector?.add((MapPos(x, y)))
            }

            _posVectorArr.add(_posVector!!)
        }

        _posVectorArr.forEachIndexed { idx, pos ->

            val polygon = Polygon(pos, setPolygonStyle(Color(255, 255, 0, _alpha), Color(0, 0, 0, 255), 2F))
            val minusNum = 1.8

            makePolygonArr.add(polygon)

            val centerPos = MapPos(polygon.geometry.centerPos.x, polygon.geometry.centerPos.y + minusNum)
            val middlePos = MapPos(centerPos.x, centerPos.y - minusNum)
            val botPos = MapPos(middlePos.x, middlePos.y - minusNum)

            val hoNmTxt = Text(centerPos, setTextStyle(Color(0, 0, 0, 255), 30F), _labelHoNmArr[idx])
            val huNumTxt = Text(middlePos, setTextStyle(Color(255, 0, 0, 255), 32F), _labelHuNumArr[idx])
            val cPoedTxt = Text(botPos, setTextStyle(Color(0, 0, 0, 255), 30F), _labelCpoedTxtArr[idx])

            _localVectorDataSource?.apply {
                add(hoNmTxt)
                add(huNumTxt)
                add(cPoedTxt)
                add(polygon)
            }

            _vecotrLayer = EditableVectorLayer(_localVectorDataSource)
            _editEventListener = EditEventListener(this@MainActivity)

            _vecotrLayer?.apply {
                vectorEditEventListener = _editEventListener
                vectorElementEventListener = _selectListener
            }

            _selectListener = VectorElementSelectEventListener(this@MainActivity, null)
            _deselectListener = VectorElementDeselectListener(_vecotrLayer)

        }

        _mapView.layers?.add(_vecotrLayer)
        _mapView.mapEventListener = _mapCustomEventListener

        utils.logI("exploedview layer count ${getLayerCount()}")
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
     * 벡터 포인트 스타일
     * @param color Color
     * @param size Float
     * @return PointStyle?
     */
    private fun setPointStyle(color: Color, size: Float): PointStyle? {
        _pointStyleBuilder = PointStyleBuilder()
        _pointStyleBuilder?.color = color
        _pointStyleBuilder?.size = size
        return _pointStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 라인 스타일
     * @param color Color
     * @param type LineJoinType
     * @param width Float
     * @return LineStyle?
     */
    private fun setLineStyle(color: Color, type: LineJoinType, width: Float): LineStyle? {
        _lineStyleBuilder = LineStyleBuilder()
        _lineStyleBuilder?.color = color
        _lineStyleBuilder?.lineJoinType = type
        _lineStyleBuilder?.width = width
        return _lineStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 텍스트 스타일
     * @param color Color
     * @param fontSize Float
     * @return TextStyle?
     */
    private fun setTextStyle(color: Color, fontSize: Float): TextStyle? {
        _textStyleBuilder = TextStyleBuilder()
        _textStyleBuilder?.color = color
        _textStyleBuilder?.fontSize = fontSize
        _textStyleBuilder?.orientationMode = BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA_GROUND
        _textStyleBuilder?.isScaleWithDPI = false

        return _textStyleBuilder?.buildStyle()
    }

    /**
     * 벡터 폴리곤 스타일
     * @param polygonColor Color
     * @param lineColor Color
     * @param lineWidth Float
     * @return PolygonStyle?
     */
    private fun setPolygonStyle(polygonColor: Color, lineColor: Color, lineWidth: Float): PolygonStyle? {
        _polygonStyleBuilder = PolygonStyleBuilder()
        _polygonStyleBuilder?.color = polygonColor
        _lineStyleBuilder = LineStyleBuilder()
        _lineStyleBuilder?.color = lineColor
        _lineStyleBuilder?.width = lineWidth
        _polygonStyleBuilder?.lineStyle = _lineStyleBuilder?.buildStyle()
        return _polygonStyleBuilder?.buildStyle()
    }

    /**
     * 폴리곤 선택, 비선택 모듈
     * @param type String
     * @param mapPos MapPos
     */
    fun togglePolygonStyle(type: String, mapPos: MapPos) {

        makePolygonArr.forEach { data ->
            if (data.geometry.centerPos == mapPos) {

                when (type) {
                    "select" -> data.style = setPolygonStyle(Color(255, 123, 0, _alpha), Color(0, 0, 0, 255), 2F)
                    "deselect" -> data.style = setPolygonStyle(Color(255, 255, 0, _alpha), Color(0, 0, 0, 255), 2F)
                }
            }
        }

        utils.logI("response => $mapPos")
    }


    /**
     * 그룹 지정 포인트 표출
     * @param mapPosArr MutableList<MapPos>
     */
    fun drawGroupBoundaryLayer(mapPosArr: MutableList<MapPos>) {

        _groupLocalVectorDataSource?.clear()

        runOnUiThread {
            if (mapPosArr.isNotEmpty()) {
                _resetButton.isEnabled = true
                _groupButton.isEnabled = true
            }
        }

        var pointSymbol: Point?

        _groupLocalVectorDataSource = LocalVectorDataSource(_proj)
        _pointGroupVertaxArr = mutableListOf()

        for (pos in mapPosArr) {
            pointSymbol = Point(pos, setPointStyle(Color(255, 0, 0, 255), 13F))

            _groupLocalVectorDataSource?.add(pointSymbol)
            _pointGroupVertaxArr.add(pos)
        }

        _groupVecotrLayer = EditableVectorLayer(_groupLocalVectorDataSource)

        _mapView.layers?.add(_groupVecotrLayer)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> addExplodeVectorElement(EditEventEnum.FLOOR_UP)
            R.id.btn_add_line -> addExplodeVectorElement(EditEventEnum.LINE_UP)
        }
    }

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
                makePolygonArr.map { _maxValArr.add(it.bounds.max.y.toInt()) /* 최대값 구하기*/ }
                resultMaxValue = Collections.max(_maxValArr).toDouble()
                _filterArr = makePolygonArr.filter { it.bounds.max.y == resultMaxValue } as MutableList<Polygon>

                _filterArr.map {

                    south = MapPos(it.bounds.min.x, it.bounds.max.y)
                    west = MapPos(it.bounds.max.x, it.bounds.max.y)

                    north = MapPos(it.bounds.max.x, it.bounds.max.y + _increaseFloorNum)
                    east = MapPos(it.bounds.min.x, it.bounds.max.y + _increaseFloorNum)

                    vector = MapPosVector()
                    vector.apply { add(south); add(west); add(north); add(east) }

                    addPolygon = Polygon(vector, setPolygonStyle(Color(255, 0, 0, _alpha), Color(0, 0, 0, 255), 2F))

                    makePolygonArr.add(addPolygon)
                    _source.add(addPolygon)

                }
            }

            EditEventEnum.LINE_UP -> {
                makePolygonArr.map { _maxValArr.add(it.bounds.max.x.toInt()) /* 최대값 구하기*/ }
                resultMaxValue = Collections.max(_maxValArr).toDouble()
                _filterArr = makePolygonArr.filter { it.bounds.max.x.toInt() == resultMaxValue.toInt() } as MutableList<Polygon>

                _filterArr.map {

                    south = MapPos(it.bounds.max.x, it.bounds.min.y)
                    west = MapPos(it.bounds.max.x + _increaseLineNum, it.bounds.min.y)

                    north = MapPos(it.bounds.max.x + _increaseLineNum, it.bounds.max.y)
                    east = MapPos(it.bounds.max.x, it.bounds.max.y)

                    vector = MapPosVector()
                    vector.apply { add(south); add(west); add(north); add(east) }

                    addPolygon = Polygon(vector, setPolygonStyle(Color(255, 0, 0, _alpha), Color(0, 0, 0, 255), 2F))

                    makePolygonArr.add(addPolygon)
                    _source.add(addPolygon)

                }
            }

        }

        _copyVecotrLayer = VectorLayer(_source)
        _mapView.layers.add(_copyVecotrLayer)

    }
}