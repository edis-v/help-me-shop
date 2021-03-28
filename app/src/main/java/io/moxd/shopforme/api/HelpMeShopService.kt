package io.moxd.shopforme.api

import androidx.lifecycle.LiveData
import androidx.room.Update
import io.moxd.shopforme.ParseDate
import io.moxd.shopforme.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface HelpMeShopService {

    @GET("api/user/{sessionId}")
   suspend fun getProfile(@Path("sessionId") sessionid : String) : Response<UserGSON>
    @FormUrlEncoded
    @PUT(  "api/user/update/{sessionId}")
   suspend fun updateProfile(@Path("sessionId")  sessionid: String,
                             @Field("name")  last :String ,
                             @Field("firstname")  first :String,
                             @Field("phone_number")  phonenumber :String,
                             @Field("Street")  Street :String,
                             @Field("plz")  plz :String,
                             @Field("City")  city :String,
                             @Field("usertype")  usertype :String

    ) : Response<UserGSON>

    @FormUrlEncoded
    @PUT(  "api/user/location/{sessionId}")
    suspend fun updateLocation(@Path("sessionId")  sessionid: String,
                              @Field("lat_point") lat : Double ,
                               @Field("long_point") long:Double

                              ) : Response<UserGSON>
    @Multipart
    @PUT(  "api/user/update/{sessionId}")
    suspend fun updateProfilePic(@Path("sessionId")  sessionid: String,
                                 @Part image : MultipartBody.Part,

                                 ) : Response<UserGSON>

    @GET("api/buylist/{sessionId}")
    suspend fun getBuyList(@Path("sessionId")  sessionid: String) : Response<List<BuyListGSON>>

    @GET("api/shop/{sessionId}")
    suspend fun getShops(@Path("sessionId")  sessionid: String) : Response<List<ShopGSON>>

    @GET("api/shop/{sessionId}/{id}")
    suspend fun getShop(@Path("sessionId")  sessionid: String , @Path("id")  id: String ) : Response<ShopGSON>

    @GET("api/items")
    suspend fun getItems() : Response<List<ItemGSON>>

    @DELETE("api/shop/delete/{sessionId}/{id}")
    suspend fun deleteShop(@Path("sessionId") sessionid: String , @Path("id") id: String) : Response<ShopGSON>

    @FormUrlEncoded
    @POST("api/shop/add ")
    suspend fun createShop(@Field("session_id") sessionid: String , @Field("buylist") id: String) : Response<ShopGSONCreate>


    @DELETE("api/buylist/delete/{sessionId}/{id}")
    suspend fun deleteBuyList(@Path("sessionId") sessionid: String , @Path("id") id: String) : Response<BuyListGSON>


    @FormUrlEncoded
    @PUT("api/shop/pay/{sessionId}/{id}")
    suspend fun  shopPayHF(@Path("sessionId") sessionid: String , @Path("id") id: String , @Field("payed")  payed : Boolean =true , @Field("finished_date") date : String = ParseDate() ): Response<ShopGSON>

    @Multipart
    @PUT("api/shop/payprove/{sessionId}/{id}")
    suspend fun shopPayHFS(@Path("sessionId") sessionid: String , @Path("id") id: String ,  @Part image : MultipartBody.Part): Response<ShopGSON>

    @Multipart
    @PUT("api/shop/doneHF/{sessionId}/{id}")
    suspend fun shopDoneHF(@Path("sessionId") sessionid: String , @Path("id") id: String,   @Part image : MultipartBody.Part ) : Response<ShopGSON>

    @Multipart
    @PUT("api/shop/doneHFS/{sessionId}/{id}")
    suspend fun shopDoneHFS(@Path("sessionId") sessionid: String , @Path("id") id: String, @Part image : MultipartBody.Part ,@Part("done") done : Boolean = true) : Response<ShopGSON>


    @POST("api/buylist/add")
    suspend fun createBuyList (@Body buylist:BuyListCreate) : Response<BuyListGSON>
    @GET("api/angebot/hfs/{sessionId}")
    suspend fun  getAngeboteHFS(@Path("sessionId") sessionid: String) : Response<List<AngebotGSON>>
    @FormUrlEncoded
    @PUT("api/angebot/hfs/{sessionId}/{id}")
    suspend fun replyAngebot(@Path("sessionId")  sessionid: String , @Path("id")  id: String, @Field("approve") approve : Boolean, @Field("viewed") viewed : Boolean = true ) : Response<AngebotGSON>

    @GET("api/angebot/hf/{sessionId}")
    suspend fun  getAngeboteHF(@Path("sessionId") sessionid: String) : Response<List<AngebotHelper>>

    @GET("api/user/search/{sessionId}")
    suspend fun getOtherUsers(@Path("sessionId") sessionid: String , @Query("radius") radius:Int) : Response<List<ShopMap>>

     @GET("api/user/search/{sessionId}")
     suspend fun getOtherUsersMax(@Path("sessionId") sessionid: String , @Query("radius") radius:Int = 100) : Response<List<ShopMap>>


    @FormUrlEncoded
    @POST("api/angebot/add")
    suspend fun createAngebot(@Field("session_id") sessionid: String,@Field("shop") shop:Int) : Response<AngebotHelper>


    @FormUrlEncoded
    @PUT("api/user/firebase/{sessionId}")
    suspend fun updateFirebase(@Path("sessionId") sessionid: String , @Field("firebase_token") token:String)



}
