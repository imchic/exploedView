package com.example.exploedview.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _liveData = MutableLiveData<Event>()
    val liveData: LiveData<Event> = _liveData

    fun setTheme(theme: String) = event(Event.SetTheme(theme))
    fun showLoadingBar(bool: Boolean) = event(Event.ShowLoadingBar(bool))
    fun showAlertDialog(data: ArrayList<String>) = event(Event.ShowAlertDialog(data))
    fun showAlertSelectDialog(data: ArrayList<String>) = event(Event.ShowAlertSelectDialog(data))
    fun showLog(tag: String, type: String, text: String) = event(Event.ShowLog(tag, type, text))

    // success, error, info, warning
    fun showSuccessMsg(text: String) = event(Event.ShowSuccessMsg(text))
    fun showErrorMsg(text: String) = event(Event.ShowErrorMsg(text))
    fun showInfoMsg(text: String) = event(Event.ShowInfoMsg(text))
    fun showWarningMsg(text: String) = event(Event.ShowWarningMsg(text))


    private fun event(event: Event) {
        viewModelScope.launch {
//            _eventFlow.emit(event)
            _liveData.value = event
        }
    }

    sealed class Event {
        data class SetTheme(val theme: String) : Event()
        data class ShowAlertDialog(val data: ArrayList<String>) : Event()
        data class ShowAlertSelectDialog(val data: ArrayList<String>) : Event()
        data class ShowLoadingBar(val isShow: Boolean) : Event()
        data class ShowLog(val tag: String, val type: String, val text: String) : Event()
        data class ShowSuccessMsg(val text: String) : Event()
        data class ShowErrorMsg(val text: String) : Event()
        data class ShowInfoMsg(val text: String) : Event()
        data class ShowWarningMsg(val text: String) : Event()

    }

}