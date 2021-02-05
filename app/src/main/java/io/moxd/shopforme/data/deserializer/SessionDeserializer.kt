package io.moxd.shopforme.data.deserializer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.ResponseDeserializable
import io.moxd.shopforme.data.dto.SessionDto

object SessionDeserializer: ResponseDeserializable<SessionDto> {
    override fun deserialize(content: String): SessionDto =
            jacksonObjectMapper().readValue(content)
}