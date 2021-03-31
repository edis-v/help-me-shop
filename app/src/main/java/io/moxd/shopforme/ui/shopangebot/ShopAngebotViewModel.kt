package io.moxd.shopforme.ui.shopangebot

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ShopAngebot

import io.moxd.shopforme.data.model.AngebotHelper
import io.moxd.shopforme.data.model.ShopGSON
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response


class ShopAngebotViewModel @AssistedInject constructor(@Assisted savedStateHandle: SavedStateHandle, val apiShopAdd: ShopAngebot) :
        ViewModel() {

    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()

    private val _angebote = MutableLiveData<Response<List<AngebotHelper>>>()

    val Angebote: LiveData<Response<List<AngebotHelper>>> = _angebote

    private val _shops = MutableLiveData<Response<List<ShopGSON>>>()

    val Shops: LiveData<Response<List<ShopGSON>>> = _shops


    init {

        getAngebotUpdate()

    }

    fun getShopsUpdate() {
        viewModelScope.launch {

            _shops.value = apiShopAdd.getShops(sessionId)

        }
    }


    fun getAngebotUpdate() {
        viewModelScope.launch {
            _angebote.value = apiShopAdd.getAngebote(sessionId)
        }
    }
}