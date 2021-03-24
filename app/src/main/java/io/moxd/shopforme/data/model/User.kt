package io.moxd.shopforme.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserType { Helfer, Hilfesuchender }

@Parcelize
@Serializable
data class User (
    val name: String,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val email: String,
    @SerialName("Street")
    val street: String,
    @SerialName("plz")
    val postalCode: Int,
    @SerialName("City")
    val city : String,
    @SerialName("profile_pic")
    val profilePic: String,
    @SerialName("usertype_txt")
    val userType: UserType = UserType.Hilfesuchender
): Parcelable


@Serializable
class UserME  (val id: Int,
               val name: String,
               val firstname :String,
               val phone_number : String,
               val email : String,
               val Street : String,
               val plz : Int,
               val City  : String,
               val profile_pic : String,
               val location : LocationData,
               val usertype_txt : String) : java.io.Serializable{

}