package io.moxd.shopforme.utils

import android.util.Log
import com.github.kittinunf.fuel.core.Response
import io.moxd.shopforme.data.model.ErrorField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import okhttp3.ResponseBody
import java.io.BufferedReader


fun getErrorRetro(response: ResponseBody?): String = runBlocking {
    withContext(Dispatchers.IO) {
        if (response != null) {
            val data = response.byteStream().bufferedReader().use(BufferedReader::readText)
            Log.d("getErrorData", data)


            return@withContext if (data.contains("non_field_errors"))
                JsonDeserializer.decodeFromString<ErrorField>(data).Error()
            else
                data.replace("[\"", "").replace("\"]", "")
        } else
            return@withContext "Empty"
    }
}

fun getError(response: Response): String = runBlocking {
    withContext(Dispatchers.IO) {
        val data = response.body().asString("application/json")
        Log.d("getErrorData", data)


        return@withContext if (data.contains("non_field_errors"))
            JsonDeserializer.decodeFromString<ErrorField>(data).Error()
        else
            data.replace("[\"", "").replace("\"]", "")
    }
}


fun getAllError(response: Response): List<String> = runBlocking {
    withContext(Dispatchers.IO) {

        val data = response.body().asString("application/json")
        Log.d("getErrorData", data)


        if (data.contains("non_field_errors"))
            return@withContext JsonDeserializer.decodeFromString<ErrorField>(data).non_field_errors
        else
            return@withContext data.replace("[\"", "").replace("\"]", "").split(",")

    }
}