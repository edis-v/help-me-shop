package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class BuyList(val id:Int , val articles: List<Article> ,val creation_date :String ) : java.io.Serializable {
}

data class BuyListGSON(val id:Int , val articles: List<Article> ,val creation_date :String ) : java.io.Serializable {
}

data class  BuyListCreate(val session_id: String, val articlesdata: List<ArticleGson>)