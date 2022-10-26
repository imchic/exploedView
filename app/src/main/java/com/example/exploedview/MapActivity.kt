package com.example.exploedview

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.base.BaseException
import com.example.exploedview.databinding.ActivityMapBinding
import com.example.exploedview.extension.repeatOnStarted
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.map.MapViewModel
import com.example.exploedview.util.LogUtil
import kotlinx.coroutines.*

class MapActivity : BaseActivity<ActivityMapBinding, MapViewModel>() {

    override val layoutId: Int = R.layout.activity_map // 레이아웃 연결
    override val vm: MapViewModel = MapViewModel()

    private val context = this@MapActivity

    override fun initViewStart() {}

    override fun initDataBinding() {
        CoroutineScope(Dispatchers.Main).launch {
            repeatOnStarted {
                vm.mapEventFlow.collect { event -> handleMapEvent(event) }
            }
            vm.setBaseMap(true)
        }
    }

    override fun initViewFinal() {

        binding.run {

            context.run {

                // 시스템 테마에 따라 테마 다르게 보여주기
                switchTheme.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) {
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
                                MapLayer.addFloor(BaseMap.addFloorDataSource, BaseMap.createPolygonArr)
                                vm.getAddFloorValue(BaseMap.getPolygonElementCnt(BaseMap.addFloorDataSource))
                                true
                            }

                            R.id.addLine -> {
                                MapLayer.addLine(BaseMap.addLineDataSource, BaseMap.createPolygonArr)
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
                                BaseMap.contains(BaseMap.createPolygonArr, BaseMap.containsPolygonArr)
                                vm.getContains(BaseMap.containsPolygonArr.size)
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
                    runOnUiThread {
                        runCatching { if (!mapEvent.flag) throw BaseException("BaseMap 생성 실패") }
                            .fold(
                                onSuccess = { BaseMap.initBaseMap(binding.cartoMapView, context, baseContext) },
                                onFailure = { LogUtil.e(it.toString()); vm.showSnackbarString(it.toString()) }
                            )
                    }
                }

                is MapViewModel.MapEvent.ClearMap -> {
                    vm.showLoadingBar(true)
                    runCatching { if (!mapEvent.flag) throw BaseException("BaseMap Object 초기화 실패") }
                        .fold(
                            onSuccess = { BaseMap.clear() },
                            onFailure = { LogUtil.e(it.toString()); vm.showSnackbarString(it.toString()) }
                        )
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