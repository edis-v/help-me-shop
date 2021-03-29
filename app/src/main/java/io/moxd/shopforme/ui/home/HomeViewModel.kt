package io.moxd.shopforme.ui.home

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiHome
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import retrofit2.Response


class HomeViewModel @AssistedInject constructor(@Assisted savedStateHandle: SavedStateHandle, val apiShopAdd: ApiHome) :
        ViewModel() {

    private val _user = MutableLiveData<Response<UserGSON>>()
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    val User: LiveData<Response<UserGSON>> = _user

    init {
        viewModelScope.launch {
            _user.value = apiShopAdd.getProfile(sessionId)
        }

    }

    fun UserType(): String {
        return _user.value?.body()?.usertype_txt!!
    }


}