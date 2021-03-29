package io.moxd.shopforme.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import io.moxd.shopforme.api.ApiProfile
import io.moxd.shopforme.api.ApiRegistration
import io.moxd.shopforme.data.UserManager.PreferencesKeys.CITY
import io.moxd.shopforme.data.UserManager.PreferencesKeys.EMAIL
import io.moxd.shopforme.data.UserManager.PreferencesKeys.FIRST_NAME
import io.moxd.shopforme.data.model.Registration
import io.moxd.shopforme.data.model.UserType2
import io.moxd.shopforme.getErrorRetro
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import io.moxd.shopforme.data.UserManager.PreferencesKeys.NAME
import io.moxd.shopforme.data.UserManager.PreferencesKeys.PHONE_NUMBER
import io.moxd.shopforme.data.UserManager.PreferencesKeys.POSTAL_CODE
import io.moxd.shopforme.data.UserManager.PreferencesKeys.STREET
import io.moxd.shopforme.data.UserManager.PreferencesKeys.USER_TYPE
import io.moxd.shopforme.data.model.UserGSON
import io.moxd.shopforme.requireAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class UserManager constructor(private val context: Context) {
    private val dataStore = context.createDataStore("user")

    private val apiProfile = ApiProfile()
    private val apiRegistration = ApiRegistration()

    // Privater Channel und öffentlicher Flow für den Zugriff von außen
    private val eventChannel = Channel<Result>()
    val events = eventChannel.receiveAsFlow()
/*
    fun profile(): UserGSON = runBlocking(Dispatchers.IO) {
        val preferences = dataStore.data.first()

        return@runBlocking UserGSON (
                name = preferences[NAME] ?: "",
                firstname = preferences[FIRST_NAME] ?: "",
                email = preferences[EMAIL] ?: "",
                phone_number = preferences[PHONE_NUMBER] ?: "",
                Street = preferences[STREET] ?: "",
                plz = preferences[POSTAL_CODE] ?: 0,
                City = preferences[CITY] ?: "",
                usertype_txt = preferences[USER_TYPE] ?: ""
        )
    }*/

    suspend fun updateProfile() {
        val sessionId = requireAuthManager().SessionID()
        val response = apiProfile.getProfile(sessionId)

        if(response.isSuccessful) {
            saveUser(response.body()!!)
            eventChannel.send(Result.ProfileUpdated)
        } else {
            eventChannel.send(Result.ProfileUpdateFailed(getErrorRetro(response.errorBody())))
        }
    }

    suspend fun register(registration: Registration) {
        val response = apiRegistration.registration(
                registration.name,
                registration.firstName,
                registration.password,
                registration.email,
                registration.phoneNumber,
                registration.address,
                registration.postalCode,
                registration.city,
                UserType2.Type[0].second
        )

        if(response.isSuccessful){
            saveUser(response.body()!!)
            eventChannel.send(Result.RegisterSuccess(registration.email, registration.password))
        } else {
            eventChannel.send(Result.RegisterError(getErrorRetro(response.errorBody())))
        }
    }

    suspend fun saveUser(user: UserGSON) {
        dataStore.edit { preferences ->
            preferences[NAME] = user.name
            preferences[FIRST_NAME] = user.firstname
            preferences[EMAIL] = user.email
            preferences[PHONE_NUMBER] = user.phone_number
            preferences[STREET] = user.Street
            preferences[POSTAL_CODE] = user.plz
            preferences[CITY] = user.City
            preferences[USER_TYPE] = user.usertype_txt
        }
    }

    private object PreferencesKeys {
        val NAME = stringPreferencesKey("name")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val STREET = stringPreferencesKey("street")
        val POSTAL_CODE = intPreferencesKey("postal_code")
        val CITY = stringPreferencesKey("city")
        val USER_TYPE = stringPreferencesKey("user_type")
    }

    sealed class Result {
        object ProfileUpdated : Result()
        data class ProfileUpdateFailed(val error: String): Result()
        data class RegisterSuccess(val email: String, val password: String) : Result()
        data class RegisterError(val error: String) : Result()
    }
}