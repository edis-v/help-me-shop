package io.moxd.shopforme.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.SESSION_ID
import io.moxd.shopforme.data.dto.SessionDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

// Einmalig erzeugte Klasse um alle Loginanfragen und die Persisitierung der Session zu managen
class AuthManager constructor(context: Context) {

    // DataStore Objekt der Nutzerdaten erstellen
    private val dataStore = context.createDataStore("user_preferences")

    // Privater Channel und öffentlicher Flow für den Zugriff von außen
    private val eventChannel = Channel<Result>()
    val events = eventChannel.receiveAsFlow()

    // Login Request + Events und Persisiterung der Session
    suspend fun login(email: String, password: String) {
        try {
            val session = Fuel.post(
                    RestPath.login,
                    listOf("email" to email, "password" to password)
            ).awaitObject<SessionDto>(kotlinxDeserializerOf(JsonDeserializer))

            dataStore.edit { preferences ->
                preferences[SESSION_ID] = session.id
            }
            eventChannel.send(Result.AuthSucess(session))
        } catch (exception: Exception) {
            // TODO: Error handling sauber umsetzen mit echten Fällen
            eventChannel.send(Result.AuthError(exception))
        }
    }

    // Login mit persisiterter Session Id
    suspend fun auth() {
        val preferences = dataStore.data.first()
        val sessionId = preferences[SESSION_ID] ?: ""
        if(sessionId.isNotEmpty()) {
            try {
                // TODO: sessionId per Request erneuern?
                Fuel.get(
                    RestPath.user(sessionId)
                ).awaitStringResponse()

                eventChannel.send(Result.AuthSucess(SessionDto(sessionId)))
            } catch (exception: Exception) {
                unauth()
                eventChannel.send(Result.AuthError(exception))
            }
        }
    }

    suspend fun unauth() {
        dataStore.edit { it.clear() }
        eventChannel.send(Result.UnauthSucess)
    }

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    sealed class Result {
        object UnauthSucess: Result()
        data class AuthSucess(val session: SessionDto) : Result()
        data class AuthError(val exception: Exception) : Result()
    }
}