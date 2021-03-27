package io.moxd.shopforme.api


import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.result.Result
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.LocationDataGSON
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.data.model.UserME
import io.moxd.shopforme.getError
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


class ApiProfile {

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://moco.fluffistar.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)

        suspend fun getProfile(sessionId: String) : Response<UserGSON> = service.getProfile(sessionId)

        suspend fun updateLocation(sessionId: String , locationDataGSON: LocationDataGSON) = service.updateLocation(sessionId , locationDataGSON)

        suspend fun updateProfilePic(sessionId: String,  image : MultipartBody.Part) = service.updateProfilePic(sessionId,image)

        suspend fun updateProfile(
            sessionId: String,
            name: String,
            firstname: String,
            phonenumber: String,
            Street: String,
            plz: String,
            City: String,
            usertype: String
        ) = service.updateProfile(
            sessionId,
            name,
            firstname,
            phonenumber,
            Street,
            plz,
            City,
            usertype
        )
}

