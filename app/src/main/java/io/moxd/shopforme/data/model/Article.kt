package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class ArticleAdd(val id : Int , val item : Int , val count :Int) {
}
@Serializable
class Article (  val item : Item2 , val count :Int) : java.io.Serializable {
}

data class ArticleAddGson(val id : Int , val item : Int , val count :Int) {
}

data class ArticleGson (  val item : Int , val count :Int) : java.io.Serializable {
}