package com.example.exploedview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploedview.databinding.ActivityNaverMapBinding
import com.example.exploedview.db.AppDatabase
import com.example.exploedview.db.BuildingInfo
import com.example.exploedview.db.BuildingInfoDao
import com.example.exploedview.map.BuildingInfoAdapter
import com.example.exploedview.map.NaverMapViewModel
import com.example.exploedview.util.LogUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NaverMapActivity : AppCompatActivity() {

    private val viewModel: NaverMapViewModel by lazy { NaverMapViewModel() }

    private lateinit var mapView: MapView
    private var locationSource: FusedLocationSource? = null
    private lateinit var naverMap: NaverMap

    var drawerLayout: DrawerLayout? = null

    // 리사이클러뷰 어댑터
    var drawerList: RecyclerView? = null
    var imgLayer: ImageView? = null
    var textLocation: TextView? = null
    var textLocationDetail: TextView? = null
    private var btnAddMarker: Button? = null
    private var btnMove: Button? = null
    private var btnCancel: Button? = null

    private var db: AppDatabase? = null
    var buildingInfoDao: BuildingInfoDao? = null

    private lateinit var adapter: BuildingInfoAdapter

    var bottomSheetDialog: BottomSheetDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_naver_map)
        val binding = DataBindingUtil.setContentView<ActivityNaverMapBinding>(
            this,
            R.layout.activity_naver_map
        )

        initViews(binding)
        initDb()
        observeViewModel()
        setupMap()
    }

    private fun initDb() {
        db = AppDatabase.getInstance(this)
        buildingInfoDao = db?.buildingInfoDao()
    }

    private fun initViews(binding: ActivityNaverMapBinding) {

        textLocation = findViewById(R.id.marker_add_location)
        drawerLayout = binding.drawerLayout
        drawerList = binding.drawerRecyclerView
        mapView = binding.naverMapView

        // bottom sheet marker
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_marker, null)

        textLocation = bottomSheetView.findViewById(R.id.marker_add_location)
        textLocationDetail = bottomSheetView.findViewById(R.id.marker_add_location_detail)

        // 데이터가 업데이트 될때마다 리사이클러뷰를 새로고침
        drawerList?.layoutManager = LinearLayoutManager(this)

        adapter = BuildingInfoAdapter(
            items = emptyList(),
            viewModel = viewModel,
            onClick = { selectedItem, position ->
                handleMenuItemClick(selectedItem, position)
            }
        )

        binding.imgLayer.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupMap() {

        mapView.getMapAsync { naverMap ->

            this.naverMap = naverMap
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.None

            val initialPosition = LatLng(35.15664464076588, 129.14510989614843)
            val initialZoom = 18.0
            naverMap.cameraPosition = CameraPosition(initialPosition, initialZoom)

            // 맵 옵션 설정
            naverMap.uiSettings.apply {
                isLocationButtonEnabled = true
                isScaleBarEnabled = true
                isZoomControlEnabled = true
                isCompassEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true
            }

            // 현 위치 이동
            naverMap.locationOverlay.apply {
                isVisible = true
                setOnClickListener {
                    naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition))
                    true
                }
            }

            // 맵 클릭 이벤트
            naverMap.setOnMapClickListener { _, coord ->
                showBottomSheet(coord, naverMap)
            }

            // 건물 정보 가져오기
            viewModel.getBuildingInfo(dao = buildingInfoDao, this@NaverMapActivity, naverMap)
        }
    }

    private fun observeViewModel() {

        viewModel.address.observe(this) { address ->

            // 주소를 기반으로 공동주택 정보 조회
            val addressParts = address.split(",")
            val originalAddress = if (addressParts.isNotEmpty()) {
                addressParts[0].trim()
            } else {
                "주소 없음"
            }
            val roadAddress = if (addressParts.size > 1) {
                addressParts[1].trim()
            } else {
                "도로명 주소 없음"
            }

            textLocation?.text = roadAddress

            // 공동주택 정보 조회 API 호출
            viewModel.findAptInfo(originalAddress, this@NaverMapActivity)
        }

        viewModel.aptInfo.observe(this) { aptInfo ->

            val lines = aptInfo.split("\n")
            val bulletText = lines.joinToString("\n") { "• $it" } // 또는 " . $it"

            textLocationDetail?.text = bulletText
        }

        viewModel.buildingInfoArray.observe(this) { info ->
            // 리사이클러뷰 어댑터에 데이터 설정
            adapter = BuildingInfoAdapter(
                items = info,
                viewModel = viewModel,
                onClick = { selectedItem, position ->
                    LogUtil.i("Selected item: $selectedItem")
                    handleMenuItemClick(selectedItem, position)
                }
            )
            drawerList?.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }

    private fun handleMenuItemClick(selectedItem: BuildingInfo, position: Int) {
        // 선택된 항목의 주소와 세부 정보를 설정
        textLocation?.text = selectedItem.address

        // 드로어 닫기
        drawerLayout?.closeDrawers()

        // position 유효성 검사 및 마커 이동 처리
        if (position in viewModel.markers.indices) {
            val selectedMarker = viewModel.markers[position]
            selectedMarker.map?.let { map ->
                try {
                    map.moveCamera(
                        CameraUpdate.scrollAndZoomTo(
                            selectedMarker.position, 18.0
                        )
                    )

                    val bottomSheetView =
                        LayoutInflater.from(this).inflate(R.layout.bottom_sheet_marker, null)
                    bottomSheetDialog = BottomSheetDialog(this).apply {
                        setContentView(bottomSheetView)
                        window?.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val textLocation =
                        bottomSheetView.findViewById<TextView>(R.id.marker_add_location)
                    val textLocationDetail =
                        bottomSheetView.findViewById<TextView>(R.id.marker_add_location_detail)

                    val btnMove = bottomSheetView.findViewById<Button>(R.id.marker_move_button)
                    val btnAddMarker =
                        bottomSheetView.findViewById<Button>(R.id.marker_add_button)

                    CoroutineScope(Dispatchers.IO).launch {
                        val buildingInfo =
                            buildingInfoDao?.getBuildingInfoByComplexPk(selectedItem.complexPk)
                        CoroutineScope(Dispatchers.Main).launch {
                            if (buildingInfo != null) {
                                btnAddMarker?.visibility = View.GONE
                            }
                        }
                    }

                    textLocation.text = selectedItem.address

                    var textLocationDetailText = """
                        주소: ${selectedItem.address}
                        단지명_도로명주소: ${selectedItem.complexNm3}
                        단지종류: ${selectedItem.complexGbCd}
                        동수: ${selectedItem.dongCnt}
                        세대수: ${viewModel.addCommaToNumber(selectedItem.unitCnt)}
                        사용승인일: ${viewModel.convertDateFormat(selectedItem.useaprDt)}
                    """.trimIndent()

                    val lines = textLocationDetailText.split("\n")
                    textLocationDetailText = lines.joinToString("\n") { "• $it" } // 또는 " . $it"

                    textLocationDetail.text = textLocationDetailText

                    btnMove?.setOnClickListener {
                        lifecycleScope.launch {
                            val buildingInfo = withContext(Dispatchers.IO) {
                                buildingInfoDao?.getBuildingInfoByComplexPk(selectedItem.complexPk)
                            }
                            if (buildingInfo == null) {
                                Toasty.error(
                                    this@NaverMapActivity,
                                    "공동주택을 먼저 추가해주세요.",
                                    Toasty.LENGTH_SHORT
                                ).show()
                            } else {
                                // mapActivity 이동
                                val intent = Intent(
                                    this@NaverMapActivity,
                                    MapActivity::class.java
                                ).apply {
                                    putExtra("seq", selectedItem.seq)
                                    putExtra("address", textLocation?.text.toString())
                                    putExtra("aptInfo", textLocationDetail?.text.toString())
                                    putExtra("complexNm1", viewModel.buildingInfo.value?.complexNm1)
                                    putExtra(
                                        "complexGbCd",
                                        viewModel.buildingInfo.value?.complexGbCd
                                    )
                                    putExtra("dongCnt", selectedItem.dongCnt)
                                    putExtra("unitCnt", selectedItem.unitCnt)
                                    putExtra("useaprDt", selectedItem.useaprDt)
                                    putExtra("complexPk", selectedItem.complexPk)
                                    putExtra("complexNm2", selectedItem.complexNm2)
                                    putExtra("complexNm3", selectedItem.complexNm3)
                                    putExtra("latitude", selectedItem.latitude)
                                    putExtra("longitude", selectedItem.longitude)
                                    putExtra("filename", selectedItem.filename)
                                }
                                startActivity(intent)
                            }
                        }
                    }

                    bottomSheetDialog?.show()

                } catch (e: Exception) {
                    LogUtil.e("Camera update failed: ${e.message}")
                }
            } ?: LogUtil.e("Marker map is null for position: $position")
        } else {
            LogUtil.e("Invalid marker position: $position")
        }
    }

    /**
     * BottomSheetDialog을 사용하여 마커 추가
     */
    fun showBottomSheet(
        coord: LatLng,
        naverMap: NaverMap,
    ) {
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_marker, null)
        bottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(bottomSheetView)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        viewModel.setCoord(coord.latitude, coord.longitude)

        viewModel.fetchNaverReverseGeoCoding(coord.latitude, coord.longitude, this@NaverMapActivity)

        btnAddMarker = bottomSheetView.findViewById(R.id.marker_add_button)
        btnMove = bottomSheetView.findViewById(R.id.marker_move_button)

        CoroutineScope(Dispatchers.IO).launch {
            val buildingInfo =
                buildingInfoDao?.getBuildingInfoByComplexPk(
                    viewModel.buildingInfo.value?.complexPk ?: ""
                )
            withContext(Dispatchers.Main) {
                if (buildingInfo != null) {
                    btnAddMarker?.visibility = View.GONE
                }
            }
        }

        textLocation = bottomSheetView.findViewById(R.id.marker_add_location)
        textLocationDetail = bottomSheetView.findViewById(R.id.marker_add_location_detail)

        btnMove?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val buildingInfo =
                    buildingInfoDao?.getBuildingInfoByComplexPk(
                        viewModel.buildingInfo.value?.complexPk ?: ""
                    )
                withContext(Dispatchers.Main) {
                    if (buildingInfo == null) {
                        Toasty.error(
                            this@NaverMapActivity,
                            "공동주택을 먼저 추가해주세요.",
                            Toasty.LENGTH_SHORT
                        ).show()
                    } else {
                        // mapActivity 이동
                        val intent = Intent(
                            this@NaverMapActivity,
                            MapActivity::class.java
                        ).apply {
                            putExtra("seq", viewModel.buildingInfo.value?.seq)
                            putExtra("address", textLocation?.text.toString())
                            putExtra("aptInfo", textLocationDetail?.text.toString())
                            putExtra(
                                "complexNm1",
                                viewModel.buildingInfo.value?.complexNm1
                            )
                            putExtra(
                                "complexGbCd",
                                viewModel.buildingInfo.value?.complexGbCd
                            )
                            putExtra("dongCnt", viewModel.buildingInfo.value?.dongCnt)
                            putExtra("unitCnt", viewModel.buildingInfo.value?.unitCnt)
                            putExtra("useaprDt", viewModel.buildingInfo.value?.useaprDt)
                            putExtra(
                                "complexPk",
                                viewModel.buildingInfo.value?.complexPk
                            )
                            putExtra(
                                "complexNm2",
                                viewModel.buildingInfo.value?.complexNm2
                            )
                            putExtra(
                                "complexNm3",
                                viewModel.buildingInfo.value?.complexNm3
                            )
                            putExtra("latitude", viewModel.buildingInfo.value?.latitude)
                            putExtra(
                                "longitude",
                                viewModel.buildingInfo.value?.longitude
                            )
                            putExtra("filename", viewModel.buildingInfo.value?.filename)
                        }
                        startActivity(intent)
                    }
                }
            }
        }

        btnAddMarker?.setOnClickListener {
            val marker = Marker().apply {
                position = coord
                icon =
                    OverlayImage.fromResource(com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue)
            }

            if (!viewModel.addMarker(marker, naverMap)) {
                Toasty.error(this, "중복된 건물명입니다.", Toasty.LENGTH_SHORT).show()
            } else {

                marker.map = naverMap
                marker.setOnClickListener {
                    textLocation?.text = viewModel.address.value
                    textLocationDetail?.text = viewModel.aptInfo.value

                    bottomSheetDialog?.show()

                    true
                }

                // ViewModel에 마커 및 데이터 추가
                viewModel.buildingInfo.value?.let { buildingInfo ->
                    BuildingInfo(
                        complexNm1 = buildingInfo.complexNm1,
                        complexGbCd = buildingInfo.complexGbCd,
                        address = buildingInfo.address,
                        dongCnt = buildingInfo.dongCnt,
                        unitCnt = buildingInfo.unitCnt,
                        latitude = buildingInfo.latitude,
                        longitude = buildingInfo.longitude,
                        complexPk = buildingInfo.complexPk,
                        complexNm2 = buildingInfo.complexNm2,
                        complexNm3 = buildingInfo.complexNm3,
                        useaprDt = buildingInfo.useaprDt,
                        filename = buildingInfo.filename
                    )
                } ?: run {
                    LogUtil.e("BuildingInfo 값이 null입니다.")
                }

                buildingInfoDao.let { dao ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.buildingInfo.value?.let { it1 ->
                            dao?.insert(
                                it1
                            )
                        }
                    }
                }

                Toasty.success(
                    this,
                    "마커가 추가되었습니다.",
                    Toasty.LENGTH_SHORT
                ).show()
            }

            bottomSheetDialog?.dismiss()
        }

        btnCancel?.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (
            locationSource?.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            ) == true
        ) {
            if (!locationSource?.isActivated!!) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // 화면 회전 등으로 액티비티가 재생성될 때, 상태를 저장
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    // 액티비티가 화면에 보여질 때
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    // 액티비티가 화면에 보여지지 않을 때
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    // 액티비티가 화면에 보여질 때
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    // 액티비티가 화면에 보여지지 않을 때
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}
