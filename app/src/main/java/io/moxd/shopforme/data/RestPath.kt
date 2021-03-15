package io.moxd.shopforme.data

class RestPath {
    companion object {
        val login = "api/user/login"
        val items = "api/items"
        val article = "api/article/add"
        fun user(sessionId: String) = "api/user/$sessionId"
        fun userUpdate(sessionId: String) = "api/user/update/$sessionId"
        val buylistadd  = "api/buylist/add"
        val angebotadd  = "api/angebot/add"
        val shopadd = "api/shop/add"
        fun buylist(sessionId: String) = "api/buylist/$sessionId"
        fun angebot(sessionId: String) = "api/angebot/hfs/$sessionId"
        fun angebotapprove(sessionId: String,id : Int) = "api/angebot/hfs/$sessionId/$id"
        fun shop(sessionId: String) = "api/shop/$sessionId"
        fun otherUsers(sessionId: String,radius: String) = "api/user/search/$sessionId?radius=$radius"
        fun locationUpdate(sessionId: String)= "api/user/location/$sessionId"
    }
}