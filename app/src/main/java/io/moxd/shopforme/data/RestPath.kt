package io.moxd.shopforme.data

class RestPath {
    companion object {
        val login = "api/user/login"
        val register = "api/user/add"
        fun firebasetoken(sessionId: String)= "api/user/firebase/$sessionId"
        fun user(sessionId: String) = "api/user/$sessionId"
        val angebotadd  = "api/angebot/add"
        fun otherUsers(sessionId: String,radius: String) = "api/user/search/$sessionId?radius=$radius"
        fun locationUpdate(sessionId: String)= "api/user/location/$sessionId"
    }
}