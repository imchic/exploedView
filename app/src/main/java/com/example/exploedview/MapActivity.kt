package com.example.exploedview

import android.view.View
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.databinding.ActivityMapBinding
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.map.MapViewModel
import com.example.exploedview.util.LogUtil
import com.google.android.material.badge.BadgeDrawable

class MapActivity : BaseActivity<ActivityMapBinding, MapViewModel>(), View.OnClickListener {

    override val _layoutResID: Int = R.layout.activity_map // 레이아웃 연결
    override val _viewModel: MapViewModel = MapViewModel()

    private val context = this@MapActivity

    private var badge: BadgeDrawable? = null

    override fun initView() {
        super.initView()
        viewDataBinding.apply {
            BaseMap.init(viewDataBinding.cartoMapView, context, baseContext, _viewModel)

            context.run {
                switchRead.setOnClickListener(this)

                navigationRail.apply {
                    val badge1 = getOrCreateBadge(R.id.addFloor)
                    val badge2 = getOrCreateBadge(R.id.addLine)
                    val badge3 = getOrCreateBadge(R.id.addHo)
                    val badge4 = getOrCreateBadge(R.id.contains)

                    badge1.isVisible = badge1.number != 0
                    badge2.isVisible = badge2.number != 0
                    badge3.isVisible = badge3.number != 0
                    badge4.isVisible = badge4.number != 0

                }

                navigationRail.setOnItemSelectedListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.addFloor -> {
                            MapLayer.addFloor(BaseMap.addFloorDataSource, BaseMap.createPolygonArr)
                            _viewModel.getAddFloorCnt.value = BaseMap.getPolygonElementCnt(BaseMap.addFloorDataSource)
                            true
                        }
                        R.id.addLine -> {
                            MapLayer.addLine(BaseMap.addLineDataSource, BaseMap.createPolygonArr)
                            _viewModel.getAddLineCnt.value = BaseMap.getPolygonElementCnt(BaseMap.addLineDataSource)
                            true
                        }
                        R.id.addHo -> {
                            //MapLayer.addHo(this@MapActivity, BaseMap.addHoDataSource)
                            MapLayer.addHoAlert(this@MapActivity)
                            _viewModel.getAddHoCnt.value = BaseMap.getPolygonElementCnt(BaseMap.addHoDataSource)
                            true
                        }
                        R.id.reset -> {
                            BaseMap.clear()
                            true
                        }
                        R.id.contains -> {
                            BaseMap.contains(BaseMap.createPolygonArr, BaseMap.containsPolygonArr)
                            _viewModel.getContainsCnt.value = BaseMap.mapViewModel.getGroupExplodedPolygon.value?.toInt()
                            true
                        }
                        else -> false
                    }
                }
            }


            // 관찰 LiveData
            _viewModel.apply {
//                getMapEvent.observe(context) { txtStatus.text = "실행된 Event $it" }
                getTotalExplodedPolygon.observe(context) { txtTotal.text = it.toString() }
                getBaseLayers.observe(context) { txtLayer.text = it.toString() }
                getSelectExplodedPolygon.observe(context) { txtSelect.text = it.toString() }
                getGroupExplodedPolygon.observe(context) { txtGroup.text = it.toString() }
                getLayerReadStatus.observe(context) { LogUtil.d(it.toString()) }
                getCoord.observe(context) { txtCoord.text = it }
                getAddFloorCnt.observe(context) { setBadgeNum(it, R.id.addFloor) }
                getAddLineCnt.observe(context) { setBadgeNum(it, R.id.addLine) }
                getAddHoCnt.observe(context) { setBadgeNum(it, R.id.addHo) }
                getContainsCnt.observe(context) { setBadgeNum(it, R.id.contains) }
            }
        }

    }

    private fun ActivityMapBinding.setBadgeNum(it: Int, id: Int) {
        badge = navigationRail.getBadge(id)
        badge?.apply {
            isVisible = true
            number = it
            if (number == 0) {
                isVisible = false
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.switch_read -> _viewModel.getLayerReadStatus.value = viewDataBinding.switchRead.isChecked
        }
    }

}