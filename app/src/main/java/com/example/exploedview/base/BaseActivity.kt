package com.example.exploedview.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.exploedview.MapActivity
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.util.LogUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar


abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {

    lateinit var viewDataBinding: T
    abstract val _layoutResID: Int
    abstract val _viewModel: R

    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeSetContentView()

        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this, _layoutResID)

        this.supportActionBar?.hide()

        baseObserving()
        initView()
        initViewModel()
        initListener()
        afterOnCreate()
    }

    protected open fun beforeSetContentView() {}
    protected open fun initView() {}
    protected open fun initViewModel() {}
    protected open fun initListener() {}
    protected open fun afterOnCreate() {}


    private fun baseObserving() {
        _viewModel.let {
            it.observeSnackbarMessage(this) {data ->
                defaultSnackBar(data = data)
                snackBar?.apply {
                    animationMode = ANIMATION_MODE_SLIDE
                    show()
                }
            }
            it.observeSnackbarMessageStr(this) {data ->
                defaultSnackBar(data = data)
                snackBar?.apply {
                    animationMode = ANIMATION_MODE_SLIDE
                    show()
                }
            }
            it.observeToastmessage(this) { data -> Toast.makeText(this, data, Toast.LENGTH_LONG).show() }
            it.observeToastMessageStr(this) { data -> Toast.makeText(this, data, Toast.LENGTH_LONG).show() }
            it.observeAlertDialog(this) { data->
                MaterialAlertDialogBuilder(this, com.example.exploedview.R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(data[0])
                    .setMessage(data[1])
                    .setCancelable(false)
                    .setIcon(com.example.exploedview.R.drawable.ic_baseline_business_24)
//                    .setNegativeButton(getString(R.string.exit)) { _, _ -> }
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            it.observeAddFloorBadge(this) {}
            it.observeAlertListDialog(this) { data, ->

                var pos = 0

                MaterialAlertDialogBuilder(this)
                    .setTitle(data[0])
                    .setCancelable(false)
                    .setSingleChoiceItems(arrayOf(data[1], data[2], data[3], data[4]), 0) { _, which -> pos = which }
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                        LogUtil.i(pos.toString())
                        try {
                            MapLayer.addHo(MapActivity(),BaseMap.addHoDataSource, pos)
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                    .show()
            }

        }
    }

    private fun defaultSnackBar(data: Any) {
        snackBar = Snackbar
            .make(findViewById(android.R.id.content), data.toString(), Snackbar.LENGTH_SHORT)
//            .setTextColor(Color.parseColor("#FFFFFF"))
//            .setBackgroundTint(Color.parseColor("#B3000000"))
//            .setActionTextColor(Color.parseColor("#9600D3"))
            .setAction("닫기") {
                snackBar?.dismiss()
            }
    }

}