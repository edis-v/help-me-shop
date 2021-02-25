package io.moxd.shopforme.ui.registration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.data.model.Registration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val state: SavedStateHandle // Objekt um Zustand des ViewModels beizubehalten
): ViewModel() {

    // Privater Channel für Events
    private val eventChannel = Channel<RegistrationEvent>()
    // Öffentlicher Flow auf Basis des EventChannels um asynchron mit dem Fragment zu kommunizieren
    val events = eventChannel.receiveAsFlow()

    fun onRegistrationConfirmClick(registration: Registration) = viewModelScope.launch {
        eventChannel.send(RegistrationEvent.PerformRegistration(registration))
    }

    // Eventübersicht (data class wenn Argumente nötig)
    sealed class RegistrationEvent {
        data class PerformRegistration(val registration: Registration): RegistrationEvent()
        object FeedbackMalformedEmail : RegistrationEvent()
        object FeedbackMalformedPhoneNumber : RegistrationEvent()
        object FeedbackPasswordTooWeak : RegistrationEvent()
        object FeedbackPasswordNotIdentical : RegistrationEvent()
        object FeedbackFieldObligatory : RegistrationEvent()
        object FeedbackAddressNotParsable : RegistrationEvent()
    }
}