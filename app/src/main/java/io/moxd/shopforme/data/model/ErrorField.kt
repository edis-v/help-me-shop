package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class ErrorField(val non_field_errors: List<String>) {

    fun Error(): String = non_field_errors.first()


}