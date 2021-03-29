package io.moxd.shopforme.ui.shopbuylist.shopadd

import android.view.contentcapture.ContentCaptureSessionId
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.ApiShopAdd
import io.moxd.shopforme.data.model.ShopGSON
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class ShopAddViewModel @AssistedInject constructor(@Assisted savedStateHandle: SavedStateHandle, val apiShopAdd: ApiShopAdd) : ViewModel() {

    private val _shop = MutableLiveData<Response<ShopGSON>>()
    val Shop: LiveData<Response<ShopGSON>> = _shop
    private val _shopdelte = MutableLiveData<Response<ShopGSON>>()
    val ShopDelte: LiveData<Response<ShopGSON>> = _shopdelte
    val sessionId: String = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    val modelid: Int = savedStateHandle["id"] ?: throw Exception("No Model ID")
    private val _user = MutableLiveData<Response<UserGSON>>()

    val User: LiveData<Response<UserGSON>> = _user

    init {
        viewModelScope.launch {
            _user.value = apiShopAdd.getProfile(sessionId)
        }
        getShopUpdate()
    }

    fun UserType(): String {
        return _user.value?.body()?.usertype_txt!!
    }

    fun deleteShop() {
        viewModelScope.launch {
            _shopdelte.value = apiShopAdd.deleteShop(sessionId, modelid.toString())
        }
    }

    fun shopDoneHF(path: String) {
        viewModelScope.launch {
            val file: File = File(path)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("bill_hf", file.name, requestFile)

            _shop.value = apiShopAdd.shopDoneHF(sessionId, modelid.toString(), body)

        }
    }

    fun shopDoneHFS(path: String) {
        viewModelScope.launch {
            val file: File = File(path)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("bill_hfs", file.name, requestFile)

            _shop.value = apiShopAdd.shopDoneHFS(sessionId, modelid.toString(), body)
        }
    }

    fun shopPayHF() {
        viewModelScope.launch {
            _shop.value = apiShopAdd.shopPayHF(sessionId, modelid.toString())
        }
    }


    fun shopPayHFS(path: String) {
        viewModelScope.launch {
            val file: File = File(path)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("bill_hf", file.name, requestFile)

            _shop.value = apiShopAdd.shopPayHFS(sessionId, modelid.toString(), body)
        }
    }


    fun getShopUpdate() {
        viewModelScope.launch {
            _shop.value = apiShopAdd.getShop(sessionId, modelid.toString())
        }

    }
}