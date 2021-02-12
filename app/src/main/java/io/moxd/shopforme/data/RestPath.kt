package io.moxd.shopforme.data

class RestPath {
    companion object {
        val login = "api/user/login"
        fun user(sessionId: String) = "api/user/$sessionId"
        fun userUpdate(sessionId: String) = "api/user/update/$sessionId"
    }
}