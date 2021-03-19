package io.moxd.shopforme.data.model


import kotlinx.serialization.Serializable

@Serializable
class Shop(val id: Int , val helpsearcher : UserME ,val creation_date : String, val buylist: BuyList , val helper: UserME? , val payed : Boolean , val done : Boolean , val bill_hf : String? , val bill_hfs : String? , val raiting :Int , val finished_date : String?) : java.io.Serializable {
}

@Serializable
class  ShopMap(val id:Int , val buylist: BuyList, val helpsearcher : UserME ,val creation_date: String)