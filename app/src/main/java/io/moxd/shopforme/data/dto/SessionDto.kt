package io.moxd.shopforme.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    @SerialName("session_id")
    val id: String
)

data class SessionGSON(

        val session_id: String
)