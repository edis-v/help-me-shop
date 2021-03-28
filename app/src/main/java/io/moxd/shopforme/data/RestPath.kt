package io.moxd.shopforme.data

class RestPath {
    companion object {
        val login = "api/user/login"
        val register = "api/user/add"

        fun user(sessionId: String) = "api/user/$sessionId"

    }
}