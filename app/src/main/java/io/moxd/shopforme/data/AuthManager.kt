package io.moxd.shopforme.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.SESSION_ID
import io.moxd.shopforme.data.deserializer.SessionDeserializer
import io.moxd.shopforme.data.dto.SessionDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

private const val API_LOGIN = "api/user/login"


// TODO: API überdenken: Auch email und passwort im datastore speichern um in Interface einzubauen?
//  Wie Error Handling lösen?
class AuthManager constructor(context: Context) {

    private val dataStore = context.createDataStore("user_auth")

    private val authEventChannel = Channel<Result>()
    val authEvent = authEventChannel.receiveAsFlow()

    suspend fun login(email: String, password: String) {
        try {
            val session = Fuel.post(
                API_LOGIN,
                listOf("email" to email, "password" to password)
            ).awaitObject(SessionDeserializer)

            dataStore.edit { preferences ->
                preferences[SESSION_ID] = session.sessionId
            }
            authEventChannel.send(Result.LoginSucessful(session))
        } catch (exception: Exception) {
            // TODO: Error handling sauber umsetzen mit echten Fällen
            authEventChannel.send(Result.LoginError(exception))
        }
    }

    suspend fun auth() {
        // TODO: sessionId per Request erneuern?
        val preferences = dataStore.data.first()
        val sessionId = preferences[SESSION_ID] ?: ""
        if(sessionId.isNotEmpty()) {
            authEventChannel.send(Result.LoginSucessful(SessionDto(sessionId)))
        }
    }

    suspend fun logout() {
        dataStore.edit { it.clear() }
        authEventChannel.send(Result.LogoutSucessful)
    }

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    sealed class Result {
        object LogoutSucessful: Result()
        data class LoginSucessful(val session: SessionDto) : Result()
        data class LoginError(val exception: Exception) : Result()
    }
}