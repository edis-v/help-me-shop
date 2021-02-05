package io.moxd.shopforme.data.deserializer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.ResponseDeserializable
import io.moxd.shopforme.data.model.User

object UserDeserializer : ResponseDeserializable<User> {
    override fun deserialize(content: String): User =
        jacksonObjectMapper().readValue(content)
}