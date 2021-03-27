package io.moxd.shopforme.api

import androidx.lifecycle.LiveData
import androidx.room.Update
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

    @PUT(  "api/user/location/{sessionId}")
    suspend fun updateLocation(@Path("sessionId")  sessionid: String,
                              @Body location :LocationDataGSON,

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


    @POST("api/article/add")
    suspend fun createArticle(@Body ArticleGson : ArticleGson) : Response<ArticleAddGson>

    @FormUrlEncoded
    @POST("api/buylist/add")
    suspend fun createBuyList (@Field("sessionId")  sessionid: String,
                               @Field("articles")  articles :IntArray) : Response<BuyListGSON>
}
