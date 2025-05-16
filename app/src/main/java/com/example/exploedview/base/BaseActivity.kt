package com.example.exploedview.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.util.LogUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@Suppress("IMPLICIT_CAST_TO_ANY")
abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {

    lateinit var binding: T

    abstract val layoutId: Int
    abstract val vm: R

    // global widget
    private var sb: Snackbar? = null
    private var pb: CircularProgressIndicator? = null

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initViewStart()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxJava observe, dataBinding observe..
     */
    abstract fun initDataBinding()

    /**
     * 가장 마지막에 호출. 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
    abstract fun initViewFinal()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this@BaseActivity

        lifecycleScope.launch {
//            vm.eventFlow.collect { event -> handleEvent(event) }
            vm.liveData.observe(this@BaseActivity) { event -> handleEvent(event) }
        }

        initViewStart()
        initDataBinding()
        initViewFinal()
    }

    private fun handleEvent(event: BaseViewModel.Event) = when (event) {

        is BaseViewModel.Event.SetTheme -> {

            // event.theme에 따라 테마 다르게 변경
            when (event.theme) {
                "light" -> {
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
                }

                "dark" -> {
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
                }

                else -> {
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }

            LogUtil.i("theme => ${event.theme}")
        }

        is BaseViewModel.Event.ShowLoadingBar -> {

            val bool = event.isShow
            LogUtil.i("LoadingBar Status => $bool")

            initWidgetUI()

            pb?.visibility = when (bool) {
                true -> View.VISIBLE
                false -> View.GONE
            }

        }

//        is BaseViewModel.Event.ShowSnackBar -> {
//            sb?.run {
//                setText(event.text)
//                animationMode = ANIMATION_MODE_SLIDE
//                duration = Snackbar.LENGTH_SHORT
//                show()
//            }
//        }
//
//        is BaseViewModel.Event.ShowSnackbarString -> {
//            sb?.run {
//                setText(event.text)
//                animationMode = ANIMATION_MODE_SLIDE
//                duration = Snackbar.LENGTH_SHORT
//                show()
//            }
//        }
//
//        is BaseViewModel.Event.ShowToast -> {
//            //Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
//        }
//
//        is BaseViewModel.Event.ShowToastString -> {
//            Toasty.info(this, event.text).show()
//        }

        is BaseViewModel.Event.ShowAlertDialog -> {
            MaterialAlertDialogBuilder(this)
                .setTitle(event.data[0])
                .setMessage(event.data[1])
                .setCancelable(false)
                .setIcon(com.example.exploedview.R.drawable.ic_baseline_business_24)
//                    .setNegativeButton(getString(R.string.exit)) { _, _ -> }
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        is BaseViewModel.Event.ShowAlertSelectDialog -> {
            var pos = 0
            val listSize = event.data.size

            MaterialAlertDialogBuilder(this)
                .setTitle(event.data[0])
                .setCancelable(false)
                .setSingleChoiceItems(
                    event.data.subList(1, listSize).toTypedArray(),
                    0
                ) { _, which -> pos = which }
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                    try {
                        MapLayer.addHo(BaseMap.addHoDataSource, pos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .show()


//            MaterialAlertDialogBuilder(this)
//                .setTitle(event.data[0])
//                .setCancelable(false)
//                .setSingleChoiceItems(
//                    arrayOf(event.data[1], event.data[2], event.data[3], event.data[4]),
//                    0
//                ) { _, which -> pos = which }
//                .setPositiveButton("확인") { dialog, _ ->
//                    dialog.dismiss()
//                    try {
//                        MapLayer.addHo(BaseMap.addHoDataSource, pos)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                .show()
        }

        is BaseViewModel.Event.ShowErrorMsg -> {
            LogUtil.i("Error Msg => ${event.text}")
            Toasty.error(this, event.text, Toast.LENGTH_LONG, true).show()
        }

        is BaseViewModel.Event.ShowInfoMsg -> {
            LogUtil.i("Info Msg => ${event.text}")
            Toasty.info(this, event.text).show()
        }

        is BaseViewModel.Event.ShowLog -> TODO()
        is BaseViewModel.Event.ShowSuccessMsg -> {
            LogUtil.i("Success Msg => ${event.text}")
            Toasty.success(this, event.text).show()
        }

        is BaseViewModel.Event.ShowWarningMsg -> {
            LogUtil.i("Warning Msg => ${event.text}")
            Toasty.warning(this, event.text).show()
        }
    }

    private fun initWidgetUI() {

        if (pb == null) {
            pb = CircularProgressIndicator(this)
            pb?.run {
                isIndeterminate = true
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
                isActivated = false
                isSaveEnabled = false
                isSaveFromParentEnabled = false
            }

            val layout = findViewById<View>(android.R.id.content).rootView as ViewGroup
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val ll = LinearLayout(this)

            ll.gravity = Gravity.CENTER
            ll.addView(pb)

            layout.addView(ll, params)

        }

        if (sb == null) sb = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)

    }

    open fun isNightModeActive(context: Context): Boolean {
        val defaultNightMode = AppCompatDelegate.getDefaultNightMode()
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true
        }
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false
        }
        val currentNightMode: Int = (context.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> return false
            Configuration.UI_MODE_NIGHT_YES -> return true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
        }
        return false
    }

}