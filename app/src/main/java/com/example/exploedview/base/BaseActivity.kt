package com.example.exploedview.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar


abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {

    lateinit var viewDataBinding: T
    abstract val _layoutResID: Int
    abstract val _viewModel: R

    private var snackBar: Snackbar? = null
    private var badge: BadgeDrawable? = null

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


    fun baseObserving() {
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
                    .setIcon(com.example.exploedview.R.drawable.ic_baseline_apartment_24)
//                    .setNegativeButton(getString(R.string.exit)) { _, _ -> }
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            it.observeAddFloorBadge(this) { data ->

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