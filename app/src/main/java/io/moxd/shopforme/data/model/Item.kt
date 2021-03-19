package io.moxd.shopforme.data.model

import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Item( val id : Int,val name:String , val cost:Double , @Contextual var anzahl: MutableLiveData<Int> = MutableLiveData<Int>(0)) : java.io.Serializable {
}


@Serializable
class Item2( val id : Int,val name:String , val cost:Double  ) : java.io.Serializable {
}