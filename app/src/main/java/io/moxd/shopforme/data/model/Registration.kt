package io.moxd.shopforme.data.model

data class Registration(
        val email: String,
        val password: String,
        val name: String,
        val firstName: String,
        val address: String,
        val postalCode: String,
        val city: String,
        val phoneNumber: String
)