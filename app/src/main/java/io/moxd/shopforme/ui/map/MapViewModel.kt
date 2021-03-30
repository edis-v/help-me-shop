package io.moxd.shopforme.ui.map

import android.location.Location
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiMap
import io.moxd.shopforme.data.model.*
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response

class MapViewModel @AssistedInject constructor(
        @Assisted savedStateHandle: SavedStateHandle,
        val apiMap: ApiMap

) : ViewModel() {
    lateinit var lastKnownLocation: Location
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    private val _user = MutableLiveData<Response<UserGSON>>()
    val User: LiveData<Response<UserGSON>> = _user

    private val _otherusers = MutableLiveData<Response<List<ShopMap>>>()
    val OtherUsers: LiveData<Response<List<ShopMap>>> = _otherusers

    private val _otherusersmax = MutableLiveData<Response<List<ShopMap>>>()
    val OtherUsersMax: LiveData<Response<List<ShopMap>>> = _otherusersmax

    private val _angebot = MutableLiveData<Response<AngebotHelper>>()
    val Angebot: LiveData<Response<AngebotHelper>> = _angebot

    init {
        viewModelScope.launch {
            //        _user.value = apiMap.getProfile(sessionId)
        }

    }

    fun updateLocation(locationDataGSON: LocationGSON) {
        viewModelScope.launch {
            _user.value = apiMap.updateLocation(sessionId, locationDataGSON.coordinates[0], locationDataGSON.coordinates[1])
        }
    }

    fun getOtherUsers(radius: Int) {
        viewModelScope.launch {
            _otherusers.value = apiMap.getOtherUsers(sessionId, radius)
        }

    }

    fun getMaxUser() {
        viewModelScope.launch {
            _otherusersmax.value = apiMap.getOtherUsersMax(sessionId)
        }
    }

    fun createAngebot(shop: Int) {
        viewModelScope.launch {
            _angebot.value = apiMap.createAngebot(sessionId, shop)
        }
    }


}

