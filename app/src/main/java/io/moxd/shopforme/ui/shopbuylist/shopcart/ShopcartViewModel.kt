package io.moxd.shopforme.ui.shopbuylist.shopcart

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiShopcart
import io.moxd.shopforme.data.model.*
import io.moxd.shopforme.utils.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response


class ShopcartViewModel @AssistedInject constructor(
        @Assisted savedStateHandle: SavedStateHandle,
        val apiShopcart: ApiShopcart

) : ViewModel() {
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    val selectedTab = savedStateHandle["page"] ?: "default"

    private val _buylist = MutableLiveData<Response<List<BuyListGSON>>>()
    val BuyList: LiveData<Response<List<BuyListGSON>>> = _buylist

    private val _buylistdelte = MutableLiveData<Response<BuyListGSON>>()
    val BuyListDelte: LiveData<Response<BuyListGSON>> = _buylistdelte

    private val _shop = MutableLiveData<Response<List<ShopGSON>>>()
    val Shop: LiveData<Response<List<ShopGSON>>> = _shop

    private val _shopcreated = MutableLiveData<Response<ShopGSONCreate>>()
    val ShopCreated: LiveData<Response<ShopGSONCreate>> = _shopcreated

    private val _location = MutableLiveData<Response<UserGSON>>()
    val Location: LiveData<Response<UserGSON>> = _location

    init {
        if (selectedTab == "default")
            getShopUpdate()
        else
            getBuyListUpdate()
    }

    fun getBuyListUpdate() {
        viewModelScope.launch {
            _buylist.value = apiShopcart.getBuyList(sessionId)

        }
    }

    fun updateLocation(locationDataGSON: LocationGSON) {
        viewModelScope.launch {

            _location.value = apiShopcart.updateLocation(sessionId, locationDataGSON.coordinates[0], locationDataGSON.coordinates[1])
        }
    }

    fun getShopUpdate() {
        viewModelScope.launch {
            _shop.value = apiShopcart.getShops(sessionId)

        }
    }


    fun delteBuylist(id: String) {
        viewModelScope.launch {
            _buylistdelte.value = apiShopcart.deleteBuyList(sessionId, id)
        }


    }

    fun createShop(id: String) {
        viewModelScope.launch {
            _shopcreated.value = apiShopcart.createShop(sessionId, id)
        }
    }


}