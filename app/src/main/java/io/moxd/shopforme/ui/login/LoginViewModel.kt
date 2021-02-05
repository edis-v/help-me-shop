package io.moxd.shopforme.ui.login

import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.dto.SessionDto
import io.moxd.shopforme.exhaustive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "LoginViewModel"

class LoginViewModel(
    private val authManager: AuthManager,
    private val state: SavedStateHandle
) : ViewModel() {

    var loginEmail = state.get<String>("loginEmail") ?: ""
        set(value) {
            field = value
            state.set("loginEmail", value)
        }
    var loginPassword = state.get<String>("loginPassword") ?: ""
        set(value) {
            field = value
            state.set("loginPassword", value)
        }

    private val loginEventChannel = Channel<LoginEvent>()
    val loginEvent = loginEventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            authManager.auth() // sessionId in dataStore?
        }
        viewModelScope.launch {
            authManager.authEvent.collect { result ->
                when(result) {
                    is AuthManager.Result.LoginSucessful -> loginEventChannel.send(LoginEvent.LoginSuccess(result.session))
                    is AuthManager.Result.LoginError -> loginEventChannel.send(LoginEvent.LoginFailed(result.exception))
                    else -> Unit
                }.exhaustive
            }
        }
    }

    fun onLoginClick() = viewModelScope.launch {
        when {
            loginEmail.isEmpty() && loginPassword.isEmpty()  -> loginEventChannel.send(LoginEvent.NoInput)
            loginEmail.isEmpty()  -> loginEventChannel.send(LoginEvent.EmptyEmail)
            loginPassword.isEmpty()  -> loginEventChannel.send(LoginEvent.EmptyPassword)
            !PatternsCompat.EMAIL_ADDRESS.matcher(loginEmail).matches() -> loginEventChannel.send(LoginEvent.MalformedEmail)
            else -> {
                loginEventChannel.send(LoginEvent.LoggingIn)
                authManager.login(loginEmail, loginPassword)
            }
        }
    }

    fun onRegisterClick() = viewModelScope.launch {
        loginEventChannel.send(LoginEvent.NavigateToRegistrationScreen)
    }

    fun onForgotPasswordClick() = viewModelScope.launch {
        loginEventChannel.send(LoginEvent.NavigateToForgotPasswordScreen)
    }

    fun onShowGuideClick() = viewModelScope.launch {
        loginEventChannel.send(LoginEvent.NavigateToGuideScreen)
    }

    sealed class LoginEvent {
        object LoggingIn: LoginEvent()
        data class LoginSuccess(val session: SessionDto): LoginEvent()
        data class LoginFailed(val exception: Exception): LoginEvent()
        object NoInput: LoginEvent()
        object EmptyEmail: LoginEvent()
        object EmptyPassword: LoginEvent()
        object MalformedEmail : LoginEvent()
        object NavigateToRegistrationScreen : LoginEvent()
        object NavigateToForgotPasswordScreen : LoginEvent()
        object NavigateToGuideScreen : LoginEvent()
    }
}