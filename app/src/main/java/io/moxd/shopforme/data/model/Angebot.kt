package io.moxd.shopforme.data.model

import kotlinx.serialization.Serializable

@Serializable
class Angebot(val id :Int , val helper :UserME ,val shop : Shop) {
}
@Serializable
class AngebotHelper(val id :Int , val helper :UserME ,val shop : Shop,val approve :Boolean , val viewed : Boolean) {
}