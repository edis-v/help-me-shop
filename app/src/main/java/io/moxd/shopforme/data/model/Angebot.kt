package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class Angebot(val id: Int, val helper: UserME, val creation_date: String, val shop: Shop) {
}

class AngebotGSON(val id: Int, val helper: UserME, val creation_date: String, val shop: Shop) {
}

class AngebotHelper(val id: Int, val helper: UserME, val shop: Shop, val approve: Boolean, val creation_date: String, val viewed: Boolean) {
}
class AngebotHelperCreate(val id: Int, val helper: Int, val shop: Int, val approve: Boolean, val creation_date: String, val viewed: Boolean) {}