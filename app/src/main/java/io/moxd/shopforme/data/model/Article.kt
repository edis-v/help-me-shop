package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class ArticleAdd(val id : Int , val item : Int , val count :Int) {
}
@Serializable
class Article (  val item : Item , val count :Int) {
}