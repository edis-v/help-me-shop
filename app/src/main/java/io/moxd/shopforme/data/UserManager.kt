package io.moxd.shopforme.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import io.moxd.shopforme.JsonDeserializer
import io.moxd.shopforme.ProtoUser
import io.moxd.shopforme.data.dto.SessionDto
import io.moxd.shopforme.data.model.User
import io.moxd.shopforme.data.proto_serializer.ProtoUserSerializer
import io.moxd.shopforme.data.proto_serializer.exists
import io.moxd.shopforme.data.proto_serializer.toModel
import io.moxd.shopforme.data.proto_serializer.toProto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UserManager (context: Context) {

    private val dataStore: DataStore<ProtoUser> = context.createDataStore (
        fileName = "user.pb",
        serializer = ProtoUserSerializer
    )

    val user: Flow<ProtoUser> = dataStore.data

    // Privater Channel und öffentlicher Flow für den Zugriff von außen
    private val eventChannel = Channel<UserEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        // Interne Events handeln
        CoroutineScope(Dispatchers.IO).launch {
            events.collect { event ->
                // Userprofil holen bei neuer Session
                when(event) {
                    is UserEvent.UserSessionInitialized -> {
                        try {
                            Log.i("GetUser", event.session.id)
                            val user = Fuel.get(
                                RestPath.user(event.session.id)
                            ).awaitObject<User>(kotlinxDeserializerOf(JsonDeserializer))
                            Log.i("ProfileFetched Send", user.toString())

                            dataStore.updateData { protoUser ->
                                user.toProto(protoUser.toBuilder())
                            }

                            eventChannel.send(UserEvent.UserProfileFetchSuccess(event.session, user))
                        } catch (exception: Exception) {
                            // TODO: Error handling sauber umsetzen mit echten Fällen
                            Log.i("UserSessionInitialized", exception.message.toString())
                            eventChannel.send(UserEvent.UserProfileFetchError(exception))
                        }
                    }
                }
            }
        }
    }

    suspend fun initSession(session: SessionDto) {
        eventChannel.send(UserEvent.UserSessionInitialized(session))
    }

    suspend fun sessionRevoked() {
        dataStore.updateData { it.toBuilder().clear().build() }
        eventChannel.send(UserEvent.UserProfileRemoved)
    }


    sealed class UserEvent {
        object UserProfileRemoved: UserEvent()
        data class UserProfileFetchSuccess(val session: SessionDto, val user: User): UserEvent()
        data class UserProfileFetchError(val exception: Exception): UserEvent()
        data class UserSessionInitialized(val session: SessionDto): UserEvent()
    }
}