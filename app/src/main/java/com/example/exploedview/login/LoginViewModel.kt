package com.example.exploedview.login

import androidx.lifecycle.viewModelScope
import com.example.exploedview.base.BaseViewModel
import com.example.exploedview.util.LogUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    private val _loginEventFlow = MutableSharedFlow<LoginEvent>()
    val loginEventFlow: SharedFlow<LoginEvent> = _loginEventFlow

    var email = ""
    var password = ""

    init {
        LogUtil.d("LoginViewModel init")
    }

    fun loginEvent(event: LoginEvent) {
        viewModelScope.launch {
            _loginEventFlow.emit(event)
        }
    }

    sealed class LoginEvent {
        data class Login(val id: String, val pw: String) : LoginEvent()
        data class LoginSuccess(val token: String) : LoginEvent()
        data class LoginFail(val msg: String) : LoginEvent()
    }
}