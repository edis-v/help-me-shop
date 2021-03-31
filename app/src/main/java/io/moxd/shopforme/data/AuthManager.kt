package io.moxd.shopforme.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import io.moxd.shopforme.*
import io.moxd.shopforme.api.ApiLogin
import io.moxd.shopforme.api.ApiProfile
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.EMAIL
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.PASSWORD
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.SESSION_ID
import io.moxd.shopforme.data.dto.SessionGSON
import io.moxd.shopforme.data.model.*
import io.moxd.shopforme.utils.getErrorRetro
import io.moxd.shopforme.utils.minutes
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.io.IOException
import kotlin.concurrent.fixedRateTimer

// Einmalig erzeugte Klasse um alle Loginanfragen und die Persisitierung der Session zu managen
class AuthManager constructor(private val context: Context) {

    private val dataStore = context.createDataStore("auth_preferences")

    private val timer = fixedRateTimer("reauth", true, 30.minutes.toLong(), 30.minutes.toLong()) {
        GlobalScope.launch {
            // Reauth jede 30 min
            auth()
        }
    }

    // Privater Channel und öffentlicher Flow für den Zugriff von außen
    private val eventChannel = Channel<Result>()
    val events = eventChannel.receiveAsFlow()

    private val apiLogin = ApiLogin()
    private val apiProfile = ApiProfile()

    suspend fun checkSessionAndConnectivity() = withContext(Dispatchers.IO) {
        val preferences = dataStore.data.first()

        preferences[SESSION_ID] ?: return@withContext sendLoginNeeded()
        preferences[EMAIL] ?: return@withContext sendLoginNeeded()
        preferences[PASSWORD] ?: return@withContext sendLoginNeeded()

        if(context.isOnline()) {
            auth()
        } else {
            eventChannel.send(Result.NoConnection)
        }
    }

    suspend fun auth() = withContext(Dispatchers.IO) {
        val preferences = dataStore.data.first()
        val email = preferences[EMAIL] ?: return@withContext sendLoginNeeded()
        val password = preferences[PASSWORD] ?: return@withContext sendLoginNeeded()

        auth(email, password)
    }

    suspend fun auth(email: String, password: String) = withContext(Dispatchers.IO) {
        val response = apiLogin.login(email, password)

        if(response.isSuccessful) {
            val session: SessionGSON = response.body()!!

            dataStore.edit { preferences ->
                preferences[SESSION_ID] = session.session_id
                preferences[EMAIL] = email
                preferences[PASSWORD] = password
            }

            eventChannel.send(Result.AuthSucess(session))
        } else {
            eventChannel.send(Result.AuthError(getErrorRetro(response.errorBody())))
        }
    }

    suspend fun unauth() {
        dataStore.edit { it.clear() }
        eventChannel.send(Result.UnauthDone)
    }

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey("session_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
    }


    fun SessionID(): String = runBlocking(Dispatchers.IO)  {
        val preferences = dataStore.data.first()
        val ssid = preferences[SESSION_ID] ?:""
        return@runBlocking ssid
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

    private val sendLoginNeeded: suspend () -> Unit = suspend {
        eventChannel.send(Result.LoginNeeded)
    }

    sealed class Result {
        object LoginNeeded: Result()
        object NoConnection : Result()
        object UnauthDone : Result()
        data class AuthSucess(val session: SessionGSON) : Result()
        data class AuthError(val error: String) : Result()
        data class RegisterSuccess(val email: String, val password: String) : Result()
        data class RegisterError(val error: String) : Result()
    }
}