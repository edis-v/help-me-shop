package io.moxd.shopforme.api


import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.result.Result
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.data.RestPath
import io.moxd.shopforme.data.model.*
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


class ApiShopcart {


        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://moco.fluffistar.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)
    suspend fun updateLocation(sessionId: String , locationDataGSON: LocationDataGSON) = service.updateLocation(sessionId , locationDataGSON)

    suspend fun getBuyList( sessionId: String) = service.getBuyList(sessionId)
    suspend fun getShops(sessionId: String) = service.getShops(sessionId)
}

class ApiShopAdd {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)
    suspend fun getProfile(sessionId: String) : Response<UserGSON> = service.getProfile(sessionId)


    suspend fun  getShop(sessionId: String , id :String) = service.getShop(sessionId,id)
}

class ApiBuyListAdd{
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)

    suspend fun  getItems() = service.getItems()

    suspend fun createArticle( articleAddGson: ArticleGson) = service.createArticle(articleAddGson)

    suspend fun  createBuyList( sessionId: String , articles : IntArray) = service.createBuyList(sessionId,articles)

}


