package com.example.exploedview.base

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.exploedview.extension.repeatOnStarted
import com.example.exploedview.map.BaseMap
import com.example.exploedview.map.MapLayer
import com.example.exploedview.util.LogUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar


@Suppress("IMPLICIT_CAST_TO_ANY")
abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {

    lateinit var binding: T

    abstract val layoutId: Int
    abstract val vm: R

    // global widget
    private var snackBar: Snackbar? = null
    private var progressBar: CircularProgressIndicator? = null

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

        repeatOnStarted {
            vm.eventFlow.collect { event -> handleEvent(event) }
        }

        initViewStart()
        initDataBinding()
        initViewFinal()
    }

    private fun handleEvent(event: BaseViewModel.Event) = when (event) {

        is BaseViewModel.Event.showLog -> {
//            when(event.type){
//                "d" ->  LogUtil.d(event.tag, event.text)
//                "e" ->  LogUtil.e(event.tag, event.text)
//                "i" ->  LogUtil.i(event.tag, event.text)
//                "v" ->  LogUtil.v(event.tag, event.text)
//                "w" ->  LogUtil.w(event.tag, event.text)
//                else -> LogUtil.d(event.tag, event.text)
//            }
        }

        is BaseViewModel.Event.ShowLoadingBar -> {

            val bool = event.isShow
            LogUtil.w("LoadingBar Status => $bool")

            initWidgetUI()

            progressBar?.visibility = when(bool){
                true -> View.VISIBLE
                false -> View.GONE
            }

        }

        is BaseViewModel.Event.ShowSnackBar -> {
            snackBar?.run {
                setText(event.text)
                animationMode = ANIMATION_MODE_SLIDE
                show()
            }
        }

        is BaseViewModel.Event.ShowSnackbarString -> {
            snackBar?.run {
                setText(event.text)
                animationMode = ANIMATION_MODE_SLIDE
                show()
            }
        }

        is BaseViewModel.Event.ShowToast -> {
            Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        }

        is BaseViewModel.Event.ShowToastString -> {
            Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        }

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
            MaterialAlertDialogBuilder(this)
                .setTitle(event.data[0])
                .setCancelable(false)
                .setSingleChoiceItems(
                    arrayOf(event.data[1], event.data[2], event.data[3], event.data[4]),
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
        }

        else -> Unit
    }

    private fun initWidgetUI() {

        if (progressBar == null) {
            LogUtil.w("프로그래스바 최초 생성 최초!!!")
            progressBar = CircularProgressIndicator(this)
            progressBar?.run {
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
            ll.addView(progressBar)

            layout.addView(ll, params)

        }

        if(snackBar == null) snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)

    }

}