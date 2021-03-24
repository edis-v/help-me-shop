package io.moxd.shopforme.ui.registration

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.PASSWORD_PATTERN
import io.moxd.shopforme.data.AuthManager
import io.moxd.shopforme.data.model.Registration
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val state: SavedStateHandle // Objekt um Zustand des ViewModels beizubehalten
): ViewModel() {

    var email = state.get<String>("email") ?: ""
        set(value) {
            field = value
            state.set("email", value)
        }
    var pw = state.get<String>("pw") ?: ""
        set(value) {
            field = value
            state.set("pw", value)
        }
    var pwConfirmed = state.get<String>("pwConfirmed") ?: ""
        set(value) {
            field = value
            state.set("pwConfirmed", value)
        }
    var lastName = state.get<String>("lastName") ?: ""
        set(value) {
            field = value
            state.set("lastName", value)
        }
    var firstName = state.get<String>("firstName") ?: ""
        set(value) {
            field = value
            state.set("firstName", value)
        }
    var address = state.get<String>("address") ?: ""
        set(value) {
            field = value
            state.set("address", value)
        }
    var phoneNum = state.get<String>("phoneNum") ?: ""
        set(value) {
            field = value
            state.set("phoneNum", value)
        }

    var noError = state.get<String>("noError") ?: true
        set(value) {
            field = value
            state.set("noError", value)
        }

    // Privater Channel für Events
    private val eventChannel = Channel<RegistrationEvent>()
    private val registerFlow = requireAuthManager().events

    // Öffentlicher Flow auf Basis des EventChannels um asynchron mit dem Fragment zu kommunizieren
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            registerFlow.collectLatest { result ->
                when(result) {
                    is AuthManager.Result.RegisterSuccess -> {
                        eventChannel.send(RegistrationEvent.Success)
                        Log.d("Success", "Success")
                    }
                    is AuthManager.Result.RegisterError -> {
                        eventChannel.send(RegistrationEvent.Error)
                        Log.d("Error", "Error")
                    }
                }
            }
        }
    }

    fun onRegistrationConfirmClick(registration: Registration) = viewModelScope.launch {
        checkRequiredFields()
        eventChannel.send(RegistrationEvent.CheckErrors(registration))
    }

    private fun checkRequiredFields() = viewModelScope.launch {
        val fields = mutableListOf<String>()

        if (email.isEmpty()) fields.add("email")
        if (pw.isEmpty()) fields.add("pw")
        if (pwConfirmed.isEmpty()) fields.add("pwConfirmed")
        if (lastName.isEmpty()) fields.add("lastName")
        if (firstName.isEmpty()) fields.add("firstName")
        if (address.isEmpty()) fields.add("address")
        if (phoneNum.isEmpty()) fields.add("phoneNum")

        if(fields.isNotEmpty()) {
            eventChannel.send(RegistrationEvent.FeedbackFieldsRequired(fields))
        }
    }

    fun checkEmail() = viewModelScope.launch {
        if(email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            eventChannel.send(RegistrationEvent.FeedbackMalformedEmail)
        }
    }

    fun checkPw() = viewModelScope.launch {
        if(pwConfirmed.isNotEmpty()) {
            if(pw != pwConfirmed) {
                eventChannel.send(RegistrationEvent.FeedbackPasswordNotIdentical)
            } else if(!PASSWORD_PATTERN.matcher(pw).matches()) {
                eventChannel.send(RegistrationEvent.FeedbackPasswordTooWeak)
            }
        }
    }

    fun checkPhoneNum() = viewModelScope.launch {
        if(phoneNum.isNotEmpty() && !Patterns.PHONE.matcher(phoneNum).matches()) {
            eventChannel.send(RegistrationEvent.FeedbackMalformedPhoneNumber)
        }
    }

    fun performRegistration() = viewModelScope.launch {
        requireAuthManager().register(Registration(email, pw, lastName, firstName, address, phoneNum))
    }

    // Eventübersicht (data class wenn Argumente nötig)
    sealed class RegistrationEvent {
        data class CheckErrors(val registration: Registration): RegistrationEvent()
        data class FeedbackFieldsRequired(val field: List<String>) : RegistrationEvent()
        object FeedbackMalformedEmail : RegistrationEvent()
        object FeedbackMalformedPhoneNumber : RegistrationEvent()
        object FeedbackPasswordTooWeak : RegistrationEvent()
        object FeedbackPasswordNotIdentical : RegistrationEvent()
        object FeedbackAddressNotParsable : RegistrationEvent()
        object Success: RegistrationEvent()
        object Error: RegistrationEvent()
    }
}