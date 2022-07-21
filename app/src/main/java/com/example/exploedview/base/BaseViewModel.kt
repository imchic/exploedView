package com.example.exploedview.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.exploedview.util.ComponentUtil
import com.example.exploedview.util.LogUtil
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {

    private val snackBarMsg = ComponentUtil.SnackbarMessage()
    private val snackBarMsgString = ComponentUtil.SnackbarMessageString()

    private val toastMsg = ComponentUtil.ToastMessage()
    private val toastMsgString = ComponentUtil.ToastMessageString()

    private val alertDialog = ComponentUtil.alertDialog()
    private val alertListDialog = ComponentUtil.alertListDialog()

    private val addFloorBadge = ComponentUtil.addFloorCntBadge()

    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        LogUtil.i("onCleared")
        super.onCleared()
    }

    fun showSnackbar(stringResourceId: Int) {
        snackBarMsg.value = stringResourceId
    }

    fun showSnackbar(str: String) {
        snackBarMsgString.value = str
    }

    fun showToast(stringResourceId: Int) {
        toastMsg.value = stringResourceId
    }

    fun showToast(str: String) {
        toastMsgString.value = str
    }

    fun showAlertDialog(data: ArrayList<String>) {
        alertDialog.value = data
    }

    fun showAlertListDialog(data: ArrayList<String>) {
        alertListDialog.value = data
    }

    fun observeSnackbarMessage(lifeCycleOwner: LifecycleOwner, ob: (Int) -> Unit) {
        snackBarMsg.observe(lifeCycleOwner, ob)
    }

    fun observeSnackbarMessageStr(lifeCycleOwner: LifecycleOwner, ob: (String) -> Unit) {
        snackBarMsgString.observe(lifeCycleOwner, ob)
    }

    fun observeToastmessage(lifeCycleOwner: LifecycleOwner, ob: (Int) -> Unit) {
        toastMsg.observe(lifeCycleOwner, ob)
    }

    fun observeToastMessageStr(lifeCycleOwner: LifecycleOwner, ob: (String) -> Unit) {
        toastMsgString.observe(lifeCycleOwner, ob)
    }

    fun observeAlertDialog(lifeCycleOwner: LifecycleOwner, ob: (ArrayList<String>) -> Unit) {
        alertDialog.observe(lifeCycleOwner, ob)
    }

    fun observeAlertListDialog(lifeCycleOwner: LifecycleOwner, ob: (ArrayList<String>) -> Unit) {
        alertListDialog.observe(lifeCycleOwner, ob)
    }

    fun observeAddFloorBadge(lifeCycleOwner: LifecycleOwner, ob: (Int) -> Unit) {
        addFloorBadge.observe(lifeCycleOwner, ob)
    }

}