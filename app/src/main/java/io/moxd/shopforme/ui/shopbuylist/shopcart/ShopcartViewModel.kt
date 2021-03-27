package io.moxd.shopforme.ui.shopbuylist.shopcart

import android.content.Context
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.adapter.BuyListAdapter
import io.moxd.shopforme.api.ApiProfile
import io.moxd.shopforme.api.ApiShopcart
import io.moxd.shopforme.data.model.BuyListGSON
import io.moxd.shopforme.data.model.LocationDataGSON
import io.moxd.shopforme.data.model.ShopGSON
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response

class ShopcartViewModel @AssistedInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    val apiShopcart: ApiShopcart

): ViewModel()  {
    val sessionId :  String  = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    val selectedTab =  savedStateHandle["page"] ?: "default"

    private val _buylist =  MutableLiveData<Response<List<BuyListGSON>>>()
    val BuyList :   LiveData<Response<List<BuyListGSON>>> = _buylist

    private val _shop =  MutableLiveData<Response<List<ShopGSON>>>()
    val Shop :   LiveData<Response<List<ShopGSON>>> = _shop

    private  val _location =  MutableLiveData<Response<UserGSON>>()
    val Location :   LiveData<Response<UserGSON>> = _location

    init {

        if(selectedTab == "default")
            getShopUpdate()
        else
            getBuyListUpdate()
    }

   fun getBuyListUpdate() {
       viewModelScope.launch {
          _buylist.value = apiShopcart.getBuyList(sessionId)

       }
   }
    fun updateLocation(locationDataGSON: LocationDataGSON){
        viewModelScope.launch {
         _location.value =  apiShopcart.updateLocation(sessionId,locationDataGSON)
        }
    }

    fun getShopUpdate() {
        viewModelScope.launch {
            _shop.value = apiShopcart.getShops(sessionId)

        }
    }


}