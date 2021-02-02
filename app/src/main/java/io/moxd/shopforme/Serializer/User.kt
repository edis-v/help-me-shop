package io.moxd.shopforme.Serializer

import androidx.annotation.Keep
import kotlinx.serialization.*
import kotlinx.serialization.json.*


@Keep
@Serializable
class UserME  (val id: Int, val name: String, val firstname :String, val phone_number : String, val email : String, val Street : String, val plz : Int, val City  : String, val profile_pic : String, val usertype_txt : String){

}