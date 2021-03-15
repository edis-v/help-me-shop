package io.moxd.shopforme.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.EMAIL
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.PASSWORD
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.SESSION_ID
import io.moxd.shopforme.data.dto.SessionDto
import io.moxd.shopforme.data.model.UserME
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

// Einmalig erzeugte Klasse um alle Loginanfragen und die Persisitierung der Session zu managen
class AuthManager constructor(context: Context) {

    // DataStore Objekt der Nutzerdaten erstellen
    private val dataStore = context.createDataStore("user_preferences")

    // Privater Channel und öffentlicher Flow für den Zugriff von außen
    private val eventChannel = Channel<Result>()
    val events = eventChannel.receiveAsFlow()

    // Login Request + Events und Persisiterung der Session
   /* suspend fun login(email: String, password: String) {
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
    }*/
    companion object {
        var User : UserME? = null
    }
    suspend fun login2(email: String, password: String) {
            unauth()
        val log = Fuel.post(
                RestPath.login,
                listOf("email" to email, "password" to password)
        ).responseString { request, response, result ->

            when (result) {
                is com.github.kittinunf.result.Result.Success -> {
                    GlobalScope.launch {
                        val session = Json.decodeFromString<SessionDto>(result.get());
                        dataStore.edit { preferences ->
                            preferences[SESSION_ID] = session.id
                            preferences[EMAIL] = email
                            preferences[PASSWORD] = password
                        }
                        auth2()
                        eventChannel.send(Result.AuthSucess(session))
                    }
                }
                is com.github.kittinunf.result.Result.Failure -> {
                    GlobalScope.launch {
                        eventChannel.send(Result.AuthError(result.getException()))
                    }
                }
            }

        }.join()


    }

    // Login mit persisiterter Session Id
    suspend fun auth() {
        val preferences = dataStore.data.first()
        val sessionId = preferences[SESSION_ID] ?: ""
        if (sessionId.isNotEmpty()) {
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

    suspend fun auth2() {
        val preferences = dataStore.data.first()
        val sessionId = preferences[SESSION_ID] ?: ""
        val loginEmail = preferences[EMAIL] ?: ""
        val loginPassword = preferences[PASSWORD] ?: ""
        if (loginEmail.isNotEmpty() and loginPassword.isNotEmpty())
            if (sessionId.isNotEmpty()) {


                Fuel.get(
                        RestPath.user(sessionId)
                ).responseString { request, response, result ->
                    when (result) {
                        is com.github.kittinunf.result.Result -> {
                            GlobalScope.launch {
                                try {


                                    User = Json.decodeFromString<UserME>(result.get());

                                    Log.d("SessionID" , sessionId)
                                    eventChannel.send(Result.AuthSucess(SessionDto(sessionId)))

                                } catch (ex: java.lang.Exception) {


                                    login2(loginEmail, loginPassword)

                                }
                            }
                        }
                        is com.github.kittinunf.result.Result.Failure -> {
                            GlobalScope.launch {

                                eventChannel.send(Result.AuthError(result.getException()))
                            }
                        }
                    }

                }


            } else {

                login2(loginEmail, loginPassword)
            }
        else
        {
            GlobalScope.launch {
            eventChannel.send(Result.AuthError( IOException()))}
        }
    }

    suspend fun unauth() {
        dataStore.edit { it.clear() }
    //    eventChannel.send(Result.UnauthSucess)
    }

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey("session_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
    }


    fun SessionID(): Flow<String> {
        return dataStore.getValueFlow(SESSION_ID, "")
    }


    //extension to get Value From DataStore
    private fun <T> DataStore<Preferences>.getValueFlow(
            key: Preferences.Key<T>,
            defaultValue: T
    ): Flow<T> {
        return this.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    preferences[key] ?: defaultValue
                }
    }

    sealed class Result {
        object UnauthSucess : Result()
        data class AuthSucess(val session: SessionDto) : Result()
        data class AuthError(val exception: Exception) : Result()
    }
}