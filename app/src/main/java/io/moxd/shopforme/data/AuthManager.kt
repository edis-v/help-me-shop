package io.moxd.shopforme.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import io.moxd.shopforme.ActitityMain
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.EMAIL
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.PASSWORD
import io.moxd.shopforme.data.AuthManager.PreferencesKeys.SESSION_ID
import io.moxd.shopforme.data.dto.SessionDto
import io.moxd.shopforme.data.model.*
import io.moxd.shopforme.data.proto_serializer.toProto
import io.moxd.shopforme.getAllError
import io.moxd.shopforme.getError
import io.moxd.shopforme.service.AlarmServiceSession
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
        var User : UserME? = null //switch not normal objects
    }
    suspend fun login2(email: String, password: String) {
             
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

                        val alarmManager = ActitityMain!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(ActitityMain as Context, AlarmServiceSession::class.java)    //create an intent for the BroadcastReceiver Class
                        val pendingIntent = PendingIntent.getBroadcast(         //create a Pending Intent (Broadcast Intent ) so the AlarmManager can execute my BroadcastReceiver
                                ActitityMain?.applicationContext, 234, intent, 0
                        )

                        alarmManager[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +  30*60*1000 ] = pendingIntent

                        auth2()
                        eventChannel.send(Result.AuthSucess(session))
                    }
                }
                is com.github.kittinunf.result.Result.Failure -> {
                    Log.d("Error", getError(response))
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
                        is com.github.kittinunf.result.Result.Success -> {
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
                            Log.d("Error", getError(response))
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

    fun register(registration: Registration) {

            Fuel.post(
                RestPath.register,
                listOf(
                    "name" to registration.name,
                    "firstname" to  registration.firstName,
                    "password" to registration.password,
                    "password2" to registration.password,
                    "email" to registration.email,
                    "phone_number" to registration.phoneNumber,
                    "Street" to registration.address,
                    "profile_pic" to null,
                    "plz" to registration.postalCode,
                    "City" to registration.city,
                    "usertype" to UserType2.Type[0].second
                )
            ).responseString { request, response, result ->
                when (result) {
                    is com.github.kittinunf.result.Result.Success -> {

                        GlobalScope.launch {
                            eventChannel.send(Result.RegisterSuccess(registration.email, registration.password))
                        }
                    }
                    is com.github.kittinunf.result.Result.Failure -> {
                        val errorMessages = getAllError(response)
                        GlobalScope.launch {
                            eventChannel.send(Result.RegisterError(result.getException(), errorMessages))
                        }
                    }
                }
            }.join()

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
        data class RegisterSuccess(val email: String, val password: String) : Result()
        data class RegisterError(val exception: Exception, val errorMessages: List<String>) : Result()
    }
}