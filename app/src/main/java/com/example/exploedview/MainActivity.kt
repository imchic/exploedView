package com.example.exploedview

import android.util.Log
import com.carto.components.Options
import com.carto.core.MapPos
import com.carto.core.MapPosVector
import com.carto.core.MapPosVectorVector
import com.carto.core.MapRange
import com.carto.datasources.LocalVectorDataSource
import com.carto.graphics.Color
import com.carto.layers.VectorLayer
import com.carto.projections.Projection
import com.carto.styles.*
import com.carto.ui.MapView
import com.carto.vectorelements.Polygon
import com.carto.vectorelements.Text
import com.example.exploedview.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private var _mapView: MapView? = null
    private var _mapOpt: Options? = null
    private var _proj: Projection? = null

    private var _localVectorDataSource: LocalVectorDataSource? = null
    private var _posVector: MapPosVector? = null
    private var _polygonStyleBuilder: PolygonStyleBuilder? = null
    private var _lineStyleBuilder: LineStyleBuilder? = null
    private var _textStyleBuilder: TextStyleBuilder? = null

    private var _vecotrLayer: VectorLayer? = null

    private var _codeArr = mutableListOf<String>()
    private var _posVectorArr = mutableListOf<MapPosVector>()

    private var _labelHoNmArr = mutableListOf<String>()
    private var _labelHuNumArr = mutableListOf<String>()
    private var _labelCpoedTxtArr = mutableListOf<String>()

    override fun initView() {
        super.initView()
        binding.apply {
            _mapView = binding.cartoMapView
            _mapOpt = _mapView?.options
            _proj = _mapOpt?.baseProjection

            // 맵 옵션
            _mapOpt?.apply {
                tiltRange = MapRange(90f, 90f) // 틸트 고정
                isRotatable = false // 회전
                isZoomGestures = false
                watermarkScale = 0.01f
            }

            _localVectorDataSource = LocalVectorDataSource(_proj)

            _mapView?.setZoom(22.toFloat(), 0.5f)
            _mapView?.setFocusPos(MapPos(10.0001, 7.5), 0.5f)

            try {
                val tempDataStr =
                    "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,7.5],[1.1826975241075102E-4,7.5],[1.1826975241075102E-4,15],[10.0001,15],[10.0001,7.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"201\",\"hhd_sum\":0,\"seq_no\":1,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0,\"coord_x\":5,\"coord_y\":11,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"39\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.111015462\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,0.0],[1.1826975241075102E-4,0.0],[1.1826975241075102E-4,7.5],[10.0001,7.5],[10.0001,0.0]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"101\",\"hhd_sum\":0,\"seq_no\":2,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0,\"coord_x\":5,\"coord_y\":4,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"37\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.111015461\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,22.5],[11.0001,22.5],[11.0001,30],[21.0001,30],[21.0001,22.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"402\",\"hhd_sum\":0E-15,\"seq_no\":4.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":26.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"44\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224349\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[11.0001,0.0],[11.0001,7.5],[21.0001,7.5],[21.0001,0.0],[11.0001,0.0]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"102\",\"hhd_sum\":0E-15,\"seq_no\":3.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":4.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"38\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224348\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,7.5],[11.0001,7.5],[11.0001,15],[21.0001,15],[21.0001,7.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"202\",\"hhd_sum\":0E-15,\"seq_no\":9.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":11.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"40\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224347\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[21.0001,15],[11.0001,15],[11.0001,22.5],[21.0001,22.5],[21.0001,15]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"302\",\"hhd_sum\":0E-15,\"seq_no\":8.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":16.000000000000000,\"coord_y\":19.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"42\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224346\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,22.5],[1.1826975241075102E-4,22.5],[1.1826975241075102E-4,30],[10.0001,30],[10.0001,22.5]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"401\",\"hhd_sum\":0E-15,\"seq_no\":5.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":5.000000000000000,\"coord_y\":26.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"43\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224344\"},{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[10.0001,15],[1.1826975241075102E-4,15],[1.1826975241075102E-4,22.5],[10.0001,22.5],[10.0001,15]]]]},\"properties\":{\"apt_no\":\"146\",\"adm_cd\":\"6000101\",\"bd_mgt_sn\":\"6000101001100930001021266\",\"buld_nm\":\"국제대진빌라\",\"buld_nm_dc\":null,\"ho_nm\":\"301\",\"hhd_sum\":0E-15,\"seq_no\":7.000000000000000,\"apt_type\":\"9\",\"nso_nm\":\"국제대진빌라\",\"nso_nm_dc\":null,\"hu_num_yn\":\"Y\",\"poed_group\":0E-15,\"coord_x\":5.000000000000000,\"coord_y\":19.000000000000000,\"poed_cd\":\"\",\"poed_nm\":\"\",\"hu_num\":\"41\",\"poed_txt\":\"058\",\"poed_nmtxt\":\"058-1\",\"budi_id\":\"2647004013356\",\"poed_grp2\":1.000000000000000,\"c_poed_cd\":\"001\",\"c_poed_txt\":\"001-1\",\"prt_nm\":\"국제대진빌라\",\"prt_nm_dc\":null,\"make_date\":null,\"stair_date\":\"2019-04-26\",\"poed_date\":\"2019-04-26\",\"stair_grp\":1.000000000000000,\"no_stair_grp\":null,\"no_stair_g\":null,\"surv_id\":\"39\",\"delete_yn\":null},\"id\":\"adm_cd_apt_no.67224345\"}]}"
                val parseJson = httpResultToJsonObject(tempDataStr)

                val getFeatures = parseJson.get("features").asJsonArray
                for (i in 0 until getFeatures.size()) {
                    _codeArr.add(getFeatures.get(i).asJsonObject.get("geometry").asJsonObject.get("coordinates").asJsonArray.get(0).asJsonArray.get(0).asJsonArray.toString())

                    val properties = getFeatures.get(i).asJsonObject.get("properties").asJsonObject
                    val hoNm: String = properties.get("ho_nm").asString
                    val huNum: String = properties.get("hu_num").asString
                    val cPoedTxt: String = properties.get("c_poed_txt").asString

                    _labelHoNmArr.add(hoNm)
                    _labelHuNumArr.add(huNum)
                    _labelCpoedTxtArr.add(cPoedTxt)

                    Log.d("imchic", "properties :: $properties")
                    Log.d("imchic", "hoNm :: $hoNm, huNum :: $huNum, c_poed_txt :: $cPoedTxt")

                }

            } catch (e: Exception) {
                Log.e("imchic", e.toString())
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

                _polygonStyleBuilder = PolygonStyleBuilder()
                _polygonStyleBuilder?.color = Color(255, 255, 0, 255)
                _lineStyleBuilder = LineStyleBuilder()
                _lineStyleBuilder?.color = Color(0, 0, 0, 255)
                _lineStyleBuilder?.width = 2F
                _polygonStyleBuilder?.lineStyle = _lineStyleBuilder?.buildStyle()

                val ply = Polygon(pos, _polygonStyleBuilder?.buildStyle())
                val minusNum = 1.8

                val centerPos = MapPos(ply.geometry.centerPos.x, ply.geometry.centerPos.y + minusNum)
                val middlePos = MapPos(centerPos.x, centerPos.y - minusNum)
                val botPos = MapPos(middlePos.x, middlePos.y - minusNum)

                Log.d("imchic", "center :: ${centerPos.x}, ${centerPos.y}")
                Log.d("imchic", "middle :: ${middlePos.x}, ${middlePos.y}")
                Log.d("imchic", "bot :: ${botPos.x}, ${botPos.y}")

                val hoNmTxt = Text(centerPos, setTextStyle(Color(0, 0, 0, 255), 30F), _labelHoNmArr[idx])
                val huNumTxt = Text(middlePos, setTextStyle(Color(255, 0, 0, 255), 30F), _labelHuNumArr[idx])
                val cPoedTxt = Text(botPos, setTextStyle(Color(0, 0, 0, 255), 30F), _labelCpoedTxtArr[idx])

                _localVectorDataSource?.apply {
                    add(hoNmTxt)
                    add(huNumTxt)
                    add(cPoedTxt)
                    add(ply)
                }

                _vecotrLayer = VectorLayer(_localVectorDataSource)
                _mapView?.layers?.add(_vecotrLayer)

            }

        }
    }

    fun setTextStyle(color: Color, fontSize: Float): TextStyle? {
        _textStyleBuilder = TextStyleBuilder()
        _textStyleBuilder?.color = color
        _textStyleBuilder?.fontSize = fontSize
        _textStyleBuilder?.orientationMode = BillboardOrientation.BILLBOARD_ORIENTATION_FACE_CAMERA
        _textStyleBuilder?.isScaleWithDPI = false
        return _textStyleBuilder?.buildStyle()
    }
}