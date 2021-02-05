package io.moxd.shopforme.data.model

enum class UserType { HELFER, HILFESUCHENDER }

data class User (
    val id: Int,
    val name: String,
    val firstName: String,
    val phoneNumber: String,
    val email: String,
    val street: String,
    val plz: Int,
    val city : String,
    val profilePic: String,
    val userType: UserType = UserType.HILFESUCHENDER
)