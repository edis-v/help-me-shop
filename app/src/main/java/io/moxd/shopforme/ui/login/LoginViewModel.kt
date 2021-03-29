package io.moxd.shopforme.ui.login

import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.data.AuthManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

class LoginViewModel(
        private val authManager: AuthManager, // Auth Manager mit Login API
        private val state: SavedStateHandle // Objekt um Zustand des ViewModels beizubehalten
) : ViewModel() {

    // State in Variablen speichern und setter definieren
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

    // Privater Channel für Events
    private val eventChannel = Channel<LoginEvent>()

    // Öffentlicher Flow auf Basis des EventChannels um asynchron mit dem Fragment zu kommunizieren
    val events = eventChannel.receiveAsFlow()


    // Beim Login Eingaben auf verschiedene Dinge checken und im Erfolgsfall einloggen (alles Events)
    fun onLoginClick() = viewModelScope.launch {
        when {
            loginEmail.isEmpty() && loginPassword.isEmpty() -> eventChannel.send(LoginEvent.NoInput)
            loginEmail.isEmpty() -> eventChannel.send(LoginEvent.EmptyEmail)
            loginPassword.isEmpty() -> eventChannel.send(LoginEvent.EmptyPassword)
            !PatternsCompat.EMAIL_ADDRESS.matcher(loginEmail).matches() -> eventChannel.send(LoginEvent.MalformedEmail)
            else -> {
                eventChannel.send(LoginEvent.LoggingIn)
                authManager.login(loginEmail, loginPassword)
            }
        }
    }

    // NavigationEvents

    fun onRegisterClick() = viewModelScope.launch {
        eventChannel.send(LoginEvent.NavigateToRegistrationScreen)
    }

    fun onForgotPasswordClick() = viewModelScope.launch {
        eventChannel.send(LoginEvent.NavigateToForgotPasswordScreen)
    }

    fun onShowGuideClick() = viewModelScope.launch {
        eventChannel.send(LoginEvent.NavigateToGuideScreen)
    }

    // Eventübersicht (data class wenn Argumente nötig)
    sealed class LoginEvent {
        object LoggingIn : LoginEvent()
        object NoInput : LoginEvent()
        object EmptyEmail : LoginEvent()
        object EmptyPassword : LoginEvent()
        object MalformedEmail : LoginEvent()
        object NavigateToRegistrationScreen : LoginEvent()
        object NavigateToForgotPasswordScreen : LoginEvent()
        object NavigateToGuideScreen : LoginEvent()
    }
}