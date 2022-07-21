package com.example.exploedview.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {

    lateinit var viewDataBinding: T
    abstract val _layoutResID: Int
    abstract val _viewModel: R

    private var toast: Toast? = null
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


    fun baseObserving() {
        _viewModel.let {
            it.observeSnackbarMessage(this) {data ->
                snackBar = Snackbar
                    .make(findViewById(android.R.id.content), data, Snackbar.LENGTH_LONG)
                    .setAction("닫기", View.OnClickListener {
                        snackBar?.dismiss()
                    })
                snackBar?.show()
            }
            it.observeSnackbarMessageStr(this) {data ->
                snackBar = Snackbar
                    .make(findViewById(android.R.id.content), data, Snackbar.LENGTH_LONG)
                    .setAction("닫기", View.OnClickListener {
                        snackBar?.dismiss()
                    })
                snackBar?.show()
            }
            it.observeToastmessage(this) { data -> Toast.makeText(this, data, Toast.LENGTH_LONG).show() }
            it.observeToastMessageStr(this) { data -> Toast.makeText(this, data, Toast.LENGTH_LONG).show() }
            it.observeAlertDialog(this) { data->
//                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//                builder.apply {
//                    setMessage(data)
//                    setTitle("정보")
//                    setCancelable(false)
//                    setPositiveButton("확인") { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    val alert: AlertDialog = create()
//                    alert.show()
//                }
                MaterialAlertDialogBuilder(this)
                    .setTitle(data[0])
                    .setMessage(data[1])
                    .setCancelable(false)
                    .setIcon(com.example.exploedview.R.drawable.ic_layer)
//                    .setNegativeButton(getString(R.string.exit)) { _, _ -> }
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

        }

    }

}