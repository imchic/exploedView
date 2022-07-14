package com.example.exploedview.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.gson.JsonObject
import com.google.gson.JsonParser


abstract class BaseActivity<T : ViewDataBinding>
    (@LayoutRes private val layoutId: Int) : AppCompatActivity() {

    protected lateinit var binding: T

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeSetContentView()

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)

        this.supportActionBar?.hide()

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

    fun showToast(msg: String?){

        if(toast == null){
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(msg)
        }
        toast?.show()

    }

    fun httpResultToJsonObject(resultStr: String): JsonObject = JsonParser.parseString(resultStr).asJsonObject

}