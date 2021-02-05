package io.moxd.shopforme.ui.registration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.shopforme.data.model.Registration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(private val state: SavedStateHandle): ViewModel() {

    private val registrationEventChannel = Channel<RegistrationEvent>()
    val registrationEvent = registrationEventChannel.receiveAsFlow()

    fun onRegistrationConfirmClick(registration: Registration) = viewModelScope.launch {
        registrationEventChannel.send(RegistrationEvent.PerformRegistration(registration))
    }

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