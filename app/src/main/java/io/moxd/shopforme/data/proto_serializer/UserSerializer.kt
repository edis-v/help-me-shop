package io.moxd.shopforme.data.proto_serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.moxd.shopforme.ProtoUser
import io.moxd.shopforme.data.model.User
import io.moxd.shopforme.data.model.UserType
import java.io.InputStream
import java.io.OutputStream

object ProtoUserSerializer : Serializer<ProtoUser> {
    override val defaultValue: ProtoUser = ProtoUser.getDefaultInstance()
    override fun readFrom(input: InputStream): ProtoUser {
        try {
            return  ProtoUser.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: ProtoUser, output: OutputStream) = t.writeTo(output)
}

fun ProtoUser.exists() = name.isNotEmpty()

fun ProtoUser.toModel() = User(
    name,
    firstName,
    phoneNumber,
    email,
    street,
    postalCode,
    city,
    profilePic,
    UserType.valueOf(userType.name)
)

fun User.toProto(builder: ProtoUser.Builder) = builder
    .setName(name)
    .setFirstName(firstName)
    .setPhoneNumber(phoneNumber)
    .setEmail(email)
    .setStreet(street)
    .setPostalCode(postalCode)
    .setCity(city)
    .setProfilePic(profilePic)
    .setUserType(ProtoUser.UserType.valueOf(userType.name))
    .build()