package io.moxd.shopforme.ui.angebot

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiAngebot
import io.moxd.shopforme.api.ApiShopcart
import io.moxd.shopforme.data.model.AngebotGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response

class AngebotViewModel @AssistedInject constructor(
        @Assisted savedStateHandle: SavedStateHandle,
        val apiAngebot: ApiAngebot

) : ViewModel() {
    private val _angebote = MutableLiveData<Response<List<AngebotGSON>>>()
    val Angebote: LiveData<Response<List<AngebotGSON>>> = _angebote
    private val _angebot = MutableLiveData<Response<AngebotGSON>>()
    val Angebot: LiveData<Response<AngebotGSON>> = _angebot
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()

    init {
        getAngebote()
    }

    fun getAngebote() {
        viewModelScope.launch {
            _angebote.value = apiAngebot.getAngebote(sessionId)
        }
    }

    fun replyAngebot(id: String, approve: Boolean) {
        viewModelScope.launch {
            _angebot.value = apiAngebot.replyAngebot(sessionId, id, approve)
        }
    }

}