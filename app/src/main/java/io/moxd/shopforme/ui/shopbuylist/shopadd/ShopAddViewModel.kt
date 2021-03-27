package io.moxd.shopforme.ui.shopbuylist.shopadd

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiShopAdd
import io.moxd.shopforme.data.model.ShopGSON
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response

class ShopAddViewModel @AssistedInject constructor( @Assisted savedStateHandle: SavedStateHandle, val apiShopAdd: ApiShopAdd) :ViewModel() {

    private val _shop =  MutableLiveData<Response<ShopGSON>>()
    val Shop : LiveData<Response<ShopGSON>> = _shop
    val sessionId :  String  = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    val modelid : Int =  savedStateHandle["id"] ?: throw Exception("No Model ID")
    private val _user =  MutableLiveData<Response<UserGSON>>()
    val User :LiveData<Response<UserGSON>> = _user
    init {
        viewModelScope.launch {
            _user.value = apiShopAdd.getProfile(sessionId)
        }
        getShopUpdate()
    }




    fun getShopUpdate(){
        viewModelScope.launch {
            _shop.value = apiShopAdd.getShop(sessionId,modelid.toString())
        }

    }
}