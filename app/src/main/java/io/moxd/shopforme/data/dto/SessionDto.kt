package io.moxd.shopforme.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SessionDto(
    @JsonProperty("session_id")
    val sessionId: String
)