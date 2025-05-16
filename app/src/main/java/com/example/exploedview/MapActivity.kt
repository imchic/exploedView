package com.example.exploedview

import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.carto.geometry.GeoJSONGeometryWriter
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.databinding.ActivityMapBinding
import com.example.exploedview.db.AppDatabase
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.BaseMap.activity
import com.example.exploedview.map.BaseMap.createPolygonArr
import com.example.exploedview.map.BaseMap.seq
import com.example.exploedview.map.MapLayer
import com.example.exploedview.map.MapViewModel
import com.example.exploedview.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MapActivity : BaseActivity<ActivityMapBinding, MapViewModel>() {

    override val layoutId: Int = R.layout.activity_map // 레이아웃 연결
    override val vm: MapViewModel = MapViewModel()

    private val context = this@MapActivity

    override fun initViewStart() {}

    override fun initDataBinding() {
        lifecycleScope.launch {
            binding.toolbar.run {
                title = "공동주택 전개도 뷰"
                setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                setNavigationOnClickListener {
                    // 뒤로가기 버튼 클릭 시
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            vm.mapEventFlow.collect { event -> handleMapEvent(event) }
        }
        vm.setBaseMap(true)
    }

    override fun initViewFinal() {

        binding.run {

            context.run {

                // 시스템 테마에 따라 테마 다르게 보여주기
                switchTheme.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        vm.setTheme("dark")
                        binding.tvTheme.text = "dark"
                    } else {
                        vm.setTheme("light")
                        binding.tvTheme.text = "light"
                    }
                }

                navigationRail.run {
                    val badge1 = getOrCreateBadge(R.id.addFloor)
                    val badge2 = getOrCreateBadge(R.id.addLine)
                    val badge3 = getOrCreateBadge(R.id.addHo)
                    val badge4 = getOrCreateBadge(R.id.contains)

                    badge1.isVisible = badge1.number != 0
                    badge2.isVisible = badge2.number != 0
                    badge3.isVisible = badge3.number != 0
                    badge4.isVisible = badge4.number != 0
                }

                vm.let { vm ->
                    navigationRail.setOnItemSelectedListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.addFloor -> {
                                MapLayer.addFloor(
                                    BaseMap.addFloorDataSource,
                                    createPolygonArr
                                )
                                vm.getAddFloorValue(BaseMap.getPolygonElementCnt(BaseMap.addFloorDataSource))
                                true
                            }

                            R.id.addLine -> {
                                MapLayer.addLine(
                                    BaseMap.addLineDataSource,
                                    createPolygonArr
                                )
                                vm.getAddLIneValue(BaseMap.getPolygonElementCnt(BaseMap.addLineDataSource))
                                true
                            }

                            R.id.addHo -> {
                                MapLayer.addHoAlert(this@MapActivity)
                                vm.getAddHo(BaseMap.getPolygonElementCnt(BaseMap.addHoDataSource))
                                true
                            }

                            R.id.reset -> {
                                vm.clearMap(true)
                                true
                            }

                            R.id.contains -> {
                                BaseMap.contains(
                                    createPolygonArr,
                                    BaseMap.containsPolygonArr
                                )
                                vm.getContains(BaseMap.containsPolygonArr.size)
                                true
                            }

                            R.id.save -> {

                                // 파일 이름 받기
                                val layoutInflater = layoutInflater
                                val dialogView =
                                    layoutInflater.inflate(R.layout.layout_new_map_filename, null)
                                val dialog = AlertDialog.Builder(activity)
                                    .setView(dialogView)
                                    .setCancelable(true)
                                    .show()

                                val editFileName =
                                    dialogView.findViewById<EditText>(R.id.edit_text_file)
                                editFileName.hint = vm.complexPk.value
                                editFileName.setText(vm.complexPk.value)
                                val btnSave =
                                    dialogView.findViewById<Button>(R.id.dialog_confirm_button)

                                btnSave.setOnClickListener {

                                    val geoJSONGeometryWriter = GeoJSONGeometryWriter()
                                    var geoJSON = ""

                                    // featureCollection 생성
                                    val featureCollection =
                                        BaseMap.createFeatureCollection(createPolygonArr)

                                    // featureCollection을 GeoJSON으로 변환
                                    geoJSON = geoJSONGeometryWriter.writeFeatureCollection(
                                        featureCollection
                                    )

                                    val filePath =
                                        "${filesDir.absolutePath}/${editFileName.text}.geojson"
                                    val file = File(filePath)

                                    // 파일이 존재한다면 삭제 후 재생성
                                    if (file.exists()) {
                                        file.delete()
                                    }

                                    file.outputStream().use { outputStream ->
                                        outputStream.write(geoJSON.toByteArray())
                                        LogUtil.d("GeoJSON file saved at: $filePath")
                                    }


                                    CoroutineScope(Dispatchers.IO).launch {

                                        var getMax =
                                            AppDatabase.getInstance(context).buildingInfoDao()
                                                .getMaxSeq()

                                        if (getMax == null) {
                                            getMax = 0
                                        }

                                        // seq
                                        val seq = getMax

                                        // complexPk
                                        val complexPk = vm.complexPk.value.toString()

                                        AppDatabase.getInstance(context).buildingInfoDao()
                                            .updateSeqByComplexPk(
                                                seq,
                                                complexPk
                                            )

                                        // ui
                                        runOnUiThread {
                                            vm.showSuccessMsg("신규 전개도 추가 완료")
                                        }

                                    }


                                    vm.showSuccessMsg("저장 완료")
                                    dialog.dismiss()


                                }

                                true
                            }

                            else -> false
                        }
                    }
                }
            }

            switchRead.setOnClickListener {
                vm.setLayerReadStatus(switchRead.isChecked)
            }

        }
    }

    /**
     * MapEvent 핸들러
     * @param mapEvent MapEvent
     */
    private fun handleMapEvent(mapEvent: MapViewModel.MapEvent) {

        binding.run {
            when (mapEvent) {

                is MapViewModel.MapEvent.GetBaseLayers -> {
                    txtLayer.text = mapEvent.layers.toString()
                }

                is MapViewModel.MapEvent.GetAddFloorCnt -> {
                    setBadgeNum(mapEvent.cnt, R.id.addFloor)
                }

                is MapViewModel.MapEvent.GetAddHoCnt -> {
                    setBadgeNum(mapEvent.cnt, R.id.addHo)
                }

                is MapViewModel.MapEvent.GetAddLineCnt -> {
                    setBadgeNum(mapEvent.cnt, R.id.addLine)
                }

                is MapViewModel.MapEvent.GetContainsCnt -> {
                    setBadgeNum(mapEvent.cnt, R.id.contains)
                }

                is MapViewModel.MapEvent.GetCoordinates -> {
                    txtCoord.text = mapEvent.coordinates
                }

                is MapViewModel.MapEvent.GetGroupExplodedPolygon -> {
                    txtGroup.text = mapEvent.cnt.toString()
                }

                is MapViewModel.MapEvent.SetLayerReadStatus -> {
                    switchRead.isChecked = mapEvent.status
                }

                is MapViewModel.MapEvent.GetSelectExplodedPolygon -> {
                    binding.txtSelect.text = mapEvent.cnt.toString()
                }

                is MapViewModel.MapEvent.GetExplodedViewLayer -> {
                    binding.txtTotal.text = mapEvent.data.toString()
                }

                is MapViewModel.MapEvent.SetBaseMap -> {

                    // intent로 받은 데이터
                    val baseContext = context.applicationContext
                    // seq
                    val address = intent.getStringExtra("address")
//                    val aptInfo = intent.getStringExtra("aptInfo")
                    val complexNm1 = intent.getStringExtra("complexNm1")
                    val complexGbCd = intent.getStringExtra("complexGbCd")
                    val dongCnt = intent.getStringExtra("dongCnt")
                    val unitCnt = intent.getStringExtra("unitCnt")
                    val useaprDt = intent.getStringExtra("useaprDt")
                    val complexPk = intent.getStringExtra("complexPk")
                    val complexNm2 = intent.getStringExtra("complexNm2")
                    val complexNm3 = intent.getStringExtra("complexNm3")
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)
                    val filename = intent.getStringExtra("filename")

                    LogUtil.d("seq: $seq")
                    LogUtil.d("address: $address")
//                    LogUtil.d("aptInfo: $aptInfo")
                    LogUtil.d("complexNm1: $complexNm1")
                    LogUtil.d("complexGbCd: $complexGbCd")
                    LogUtil.d("dongCnt: $dongCnt")
                    LogUtil.d("unitCnt: $unitCnt")
                    LogUtil.d("useaprDt: $useaprDt")
                    LogUtil.d("complexPk: $complexPk")
                    LogUtil.d("complexNm2: $complexNm2")
                    LogUtil.d("complexNm3: $complexNm3")
                    LogUtil.d("latitude: $latitude")
                    LogUtil.d("longitude: $longitude")
                    LogUtil.d("filename: $filename")

                    vm.seq.value = seq.toString()
                    vm.complexPk.value = complexPk.toString()

                    runOnUiThread {
                        runCatching { if (!mapEvent.flag) throw BaseException("BaseMap 생성 실패") }
                            .fold(
                                onSuccess = {
                                    BaseMap.initBaseMap(
                                        complexPk,
                                        binding.cartoMapView,
                                        context,
                                        baseContext
                                    )
                                },
                                onFailure = { LogUtil.e(it.toString()); vm.showErrorMsg(it.toString()) }
                            )
                    }
                }

                is MapViewModel.MapEvent.ClearMap -> {
                    vm.showLoadingBar(true)
                    runCatching { if (!mapEvent.flag) throw BaseException("BaseMap Object 초기화 실패") }
                        .fold(
                            onSuccess = { BaseMap.clear(); },
                            onFailure = { LogUtil.e(it.toString()); vm.showErrorMsg(it.toString()) }
                        )
                }

                is MapViewModel.MapEvent.SaveMap -> {
//                    vm.showLoadingBar(true)
//                    runCatching { if (!mapEvent.flag.isNotEmpty()) throw BaseException("저장할 데이터 없음") }
//                        .fold(
//                            onSuccess = {
//                                BaseMap.saveMap(
//                                    mapEvent.flag,
//                                    mapEvent.addFloorDataSource,
//                                    mapEvent.addLineDataSource,
//                                    mapEvent.addHoDataSource
//                                )
//                            },
//                            onFailure = { LogUtil.e(it.toString()); vm.showErrorMsg(it.toString()) }
//                        )
                }
            }
        }

    }

    /**
     * 뱃지 숫자 변경
     * @param cnt Int
     * @param resId Int
     */
    private fun setBadgeNum(cnt: Int, resId: Int) {
        binding.navigationRail.getOrCreateBadge(resId).apply {
            isVisible = true
            number = cnt
            if (number == 0) {
                isVisible = false
            }
        }
    }

}