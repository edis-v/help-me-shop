package io.moxd.shopforme.api


import io.moxd.shopforme.data.model.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


sealed class Api {
    val okHttpClient = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    protected val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://moco.fluffistar.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    protected val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)
}

class ApiProfile : Api() {


    suspend fun getProfile(sessionId: String): Response<UserGSON> = service.getProfile(sessionId)


    suspend fun updateProfilePic(sessionId: String, image: MultipartBody.Part) = service.updateProfilePic(sessionId, image)

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


class ApiShopcart : Api() {


    suspend fun updateLocation(sessionId: String, lat: Double, long: Double) = service.updateLocation(sessionId, lat, long)

    suspend fun getBuyList(sessionId: String) = service.getBuyList(sessionId)
    suspend fun getShops(sessionId: String) = service.getShops(sessionId)


    suspend fun deleteBuyList(sessionId: String, id: String) = service.deleteBuyList(sessionId, id)
    suspend fun createShop(sessionId: String, buylist: String) = service.createShop(sessionId, buylist)


}

class ApiShopAdd : Api() {
    suspend fun getProfile(sessionId: String): Response<UserGSON> = service.getProfile(sessionId)

    suspend fun deleteShop(sessionId: String, id: String) = service.deleteShop(sessionId, id)

    suspend fun shopDoneHF(sessionId: String, id: String, image: MultipartBody.Part) = service.shopDoneHF(sessionId, id, image)

    suspend fun shopDoneHFS(sessionId: String, id: String, image: MultipartBody.Part) = service.shopDoneHFS(sessionId, id, image)

    suspend fun shopPayHF(sessionId: String, id: String) = service.shopPayHF(sessionId, id)

    suspend fun shopPayHFS(sessionId: String, id: String, image: MultipartBody.Part) = service.shopPayHFS(sessionId, id, image)

    suspend fun getShop(sessionId: String, id: String) = service.getShop(sessionId, id)


}

class ApiBuyListAdd : Api() {

    suspend fun getItems() = service.getItems()


    suspend fun createBuyList(buyListCreate: BuyListCreate) = service.createBuyList(buyListCreate)

}

class ApiAngebot : Api() {

    suspend fun getAngebote(sessionId: String) = service.getAngeboteHFS(sessionId)

    suspend fun replyAngebot(sessionId: String, id: String, approve: Boolean) = service.replyAngebot(sessionId, id, approve)
}

class ApiHome : Api() {
    suspend fun getProfile(sessionId: String): Response<UserGSON> = service.getProfile(sessionId)

}


class ShopAngebot : Api() {
    suspend fun getShops(sessionId: String) = service.getShops(sessionId)

    suspend fun getAngebote(sessionId: String) = service.getAngeboteHF(sessionId)
}

class ApiMap : Api() {

    suspend fun getOtherUsers(sessionId: String, radius: Int) = service.getOtherUsers(sessionId, radius)
    suspend fun getOtherUsersMax(sessionId: String) = service.getOtherUsersMax(sessionId)

    //api create Angebot
    suspend fun createAngebot(sessionId: String, id: Int) = service.createAngebot(sessionId, id)

    suspend fun updateLocation(sessionId: String, lat: Double, long: Double) = service.updateLocation(sessionId, lat, long)


    suspend fun getProfile(sessionId: String): Response<UserGSON> = service.getProfile(sessionId)

}

class ApiFirebase : Api() {
    suspend fun updateToken(sessionId: String, token: String) = service.updateFirebase(sessionId, token)
}


class ApiLogin : Api(){
    suspend fun login(email: String, password: String) = service.login(email, password)
}

class ApiRegistration : Api(){
    suspend fun registration(
            name: String,
            firstname: String,
            password: String,
            email: String,
            phoneNumber: String,
            street: String,
            postalCode: String,
            city: String,
            userType: String
    ) = service.registration(name, firstname, password, password, email, phoneNumber, street, null, postalCode, city, userType)
}
