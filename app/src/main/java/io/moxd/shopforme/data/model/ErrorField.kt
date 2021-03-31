package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class ErrorField(val non_field_errors: List<String>? = null, val email : List<String>?  = null, val phone_number : List<String>? = null ) {

    fun Error(): String {

        if(!non_field_errors.isNullOrEmpty() )
           return non_field_errors.first()
        else if (!email.isNullOrEmpty())
            return email.first()
        else if (!phone_number.isNullOrEmpty())
            return  phone_number.first()
        else
            return  ""



    }


}