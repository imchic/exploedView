package com.example.exploedview

import android.view.View
import com.example.exploedview.base.BaseActivity
import com.example.exploedview.databinding.ActivityMainBinding
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer

class MapActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), View.OnClickListener {

    override fun initView() {
        super.initView()

        binding.apply {

            BaseMap.init(binding.cartoMapView, this@MapActivity, baseContext)

            this@MapActivity.run {
                btnAddFloor.setOnClickListener(this)
                btnAddLine.setOnClickListener(this)
                btnArea.setOnClickListener(this)
                btnReset.setOnClickListener(this)
                btnAddHo.setOnClickListener(this)
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_floor -> MapLayer.addFloor(BaseMap.floorUpDataSource, BaseMap.createPolygonArr)
            R.id.btn_add_line -> MapLayer.addLine(BaseMap.addLineDataSource, BaseMap.createPolygonArr)
            R.id.btn_area -> BaseMap.contains(BaseMap.createPolygonArr, BaseMap.containsPolygonArr)
            R.id.btn_reset -> BaseMap.clear()
            R.id.btn_add_ho -> MapLayer.addHo(this@MapActivity, BaseMap.addHoDataSource)
        }
    }

}