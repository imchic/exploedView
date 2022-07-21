package com.example.exploedview

import android.view.View
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.databinding.ActivityMainBinding
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.map.MapViewModel
import com.example.exploedview.util.LogUtil

class MapActivity : BaseActivity<ActivityMainBinding, MapViewModel>(), View.OnClickListener {

    override val _layoutResID: Int = R.layout.activity_main // 레이아웃 연결
    override val _viewModel: MapViewModel = MapViewModel()

    private val context = this@MapActivity

    override fun initView() {
        super.initView()
        viewDataBinding.apply {

            BaseMap.init(viewDataBinding.cartoMapView, context, baseContext, _viewModel)

            context.run {
                btnAddFloor.setOnClickListener(this)
                btnAddLine.setOnClickListener(this)
                btnArea.setOnClickListener(this)
                btnReset.setOnClickListener(this)
                switchRead.setOnClickListener(this)
            }

            // 관찰 LiveData
            _viewModel.apply {
//                getMapEvent.observe(context) { txtStatus.text = "실행된 Event $it" }
                getTotalExplodedPolygon.observe(context) { txtTotal.text = it }
                getBaseLayers.observe(context) { txtLayer.text = it }
                getSelectExplodedPolygon.observe(context) { txtSelect.text = it }
                getGroupExplodedPolygon.observe(context) { txtGroup.text = it }
                getLayerReadStatus.observe(context) { LogUtil.d(it.toString()) }
            }

        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> {
                MapLayer.addFloor(BaseMap.floorUpDataSource, BaseMap.createPolygonArr)
            }
            R.id.btn_add_line -> {
                MapLayer.addLine(BaseMap.addLineDataSource, BaseMap.createPolygonArr)
            }
            R.id.btn_area -> {
                BaseMap.contains(BaseMap.createPolygonArr, BaseMap.containsPolygonArr)
            }
            R.id.btn_reset -> {
                BaseMap.clear()
            }
            R.id.switch_read -> {
                _viewModel.getLayerReadStatus.value = viewDataBinding.switchRead.isChecked
            }
        }
    }

}