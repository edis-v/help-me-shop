package io.moxd.shopforme.ui.profile



import android.R.attr.path
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.moxd.shopforme.api.*
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import java.net.URI


class ProfileViewModel @AssistedInject  constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    val apiProfile: ApiProfile

):ViewModel() {
    val sessionId :  String  = savedStateHandle["ssid"] ?: requireAuthManager().SessionID()
    private  val _user  = MutableLiveData<Response<UserGSON>>()
    private val _edit = MutableLiveData<Boolean> ()
    val edit : LiveData<Boolean> = _edit
    val user : LiveData<Response<UserGSON>> = _user



    init {
        viewModelScope.launch {
            _user.value =  apiProfile.getProfile(sessionId)
            _edit.value = false
        }
    }
    fun getProfile(){
        viewModelScope.launch {
            _user.value = apiProfile.getProfile(sessionId)
        }
    }



    fun uploadimg(uri: String){
        viewModelScope.launch {
            val file: File = File(uri)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
            _user.value =  apiProfile.updateProfilePic(sessionId, body)


        }
    }



    fun OnCancelClicked(){
        viewModelScope.launch {
            _edit.value = !_edit.value!!
            _user.value =  apiProfile.getProfile(sessionId)

        }
    }


    fun OnEditClicked(){
        viewModelScope.launch {

            _edit.value = !_edit.value!!
        }
    }

    fun OnUpdateClicked(
        name: String,
        firstname: String,
        phonenumber: String,
        Street: String,
        plz: String,
        City: String,
        usertype: String
    ){
        viewModelScope.launch {
           _user.value = apiProfile.updateProfile(
               sessionId,
               name,
               firstname,
               phonenumber,
               Street,
               plz,
               City,
               usertype
           )
           _edit.value = !_edit.value!!
        }
    }

}