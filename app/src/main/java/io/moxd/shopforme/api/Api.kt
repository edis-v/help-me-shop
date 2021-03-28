package io.moxd.shopforme.api


import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.BodyLength
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
import retrofit2.http.Path
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
    suspend fun updateLocation(sessionId: String , lat:Double , long:Double) = service.updateLocation(sessionId , lat,long)

    suspend fun getBuyList( sessionId: String) = service.getBuyList(sessionId)
    suspend fun getShops(sessionId: String) = service.getShops(sessionId)


    suspend fun deleteBuyList(sessionId: String,id: String) = service.deleteBuyList(sessionId,id)
    suspend fun createShop(sessionId: String,buylist:String) = service.createShop(sessionId,buylist)


}

class ApiShopAdd {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)
    suspend fun getProfile(sessionId: String) : Response<UserGSON> = service.getProfile(sessionId)

    suspend fun deleteShop(sessionId: String,id: String) = service.deleteShop(sessionId,id)

    suspend fun shopDoneHF(sessionId: String,id: String, image: MultipartBody.Part) = service.shopDoneHF(sessionId,id,image)

    suspend fun shopDoneHFS(sessionId: String,id: String, image: MultipartBody.Part) = service.shopDoneHFS(sessionId,id,image)

    suspend fun shopPayHF(sessionId: String,id: String) = service.shopPayHF(sessionId,id)

    suspend fun shopPayHFS(sessionId: String,id: String,  image: MultipartBody.Part) = service.shopPayHFS(sessionId,id,image)

    suspend fun  getShop(sessionId: String , id :String) = service.getShop(sessionId,id)


}

class ApiBuyListAdd{
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)

    suspend fun  getItems() = service.getItems()


    suspend fun  createBuyList( buyListCreate: BuyListCreate) = service.createBuyList( buyListCreate)

}
class ApiAngebot{
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)


    suspend fun getAngebote(sessionId: String) = service.getAngeboteHFS(sessionId)

    suspend fun replyAngebot(sessionId: String,id: String,approve:Boolean) = service.replyAngebot(sessionId,id,approve)
}

class ApiHome{
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)
    suspend fun getProfile(sessionId: String) : Response<UserGSON> = service.getProfile(sessionId)

}


class ShopAngebot{
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://moco.fluffistar.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)

    suspend fun getShops(sessionId: String) = service.getShops(sessionId)

    suspend fun getAngebote(sessionId: String) = service.getAngeboteHF(sessionId)
}

class ApiMap{
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://moco.fluffistar.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)


    suspend fun getOtherUsers(sessionId: String,radius:Int) = service.getOtherUsers(sessionId,radius)
    suspend fun getOtherUsersMax(sessionId: String ) = service.getOtherUsersMax(sessionId )

    //api create Angebot
    suspend fun createAngebot(sessionId: String, id:Int) = service.createAngebot(sessionId,id)

    suspend fun updateLocation(sessionId: String , lat:Double , long:Double) = service.updateLocation(sessionId , lat , long)


    suspend fun getProfile(sessionId: String) : Response<UserGSON> = service.getProfile(sessionId)

}

class ApiFirebase{
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://moco.fluffistar.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val service: HelpMeShopService = retrofit.create(HelpMeShopService::class.java)

    suspend fun updateToken(sessionId: String,token:String) = service.updateFirebase(sessionId,token)
}



