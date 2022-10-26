package com.example.exploedview.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun setTheme(theme: String) = event(Event.SetTheme(theme))
    fun showLoadingBar(bool: Boolean) = event(Event.ShowLoadingBar(bool))
    fun showSnackbar(stringResourceId: Int) = event(Event.ShowSnackBar(stringResourceId))
    fun showSnackbarString(str: String) = event(Event.ShowSnackbarString(str))
    fun showToast(stringResourceId: Int) = event(Event.ShowToast(stringResourceId))
    fun showToastString(str: String) = event(Event.ShowToastString(str))
    fun showAlertDialog(data: ArrayList<String>) = event(Event.ShowAlertDialog(data))
    fun showAlertListDialog(data: ArrayList<String>) = event(Event.ShowAlertSelectDialog(data))
    fun showLog(tag: String, type: String, text: String) = event(Event.showLog(tag, type, text))

    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    sealed class Event {
        data class SetTheme(val theme: String) : Event()
        data class ShowSnackBar(val text: Int) : Event()
        data class ShowSnackbarString(val text: String) : Event()
        data class ShowToast(val text: Int) : Event()
        data class ShowToastString(val text: String) : Event()
        data class ShowAlertDialog(val data: ArrayList<String>) : Event()
        data class ShowAlertSelectDialog(val data: ArrayList<String>) : Event()
        data class ShowLoadingBar(val isShow: Boolean) : Event()
        data class showLog(val tag: String, val type: String, val text: String) : Event()

    }

}