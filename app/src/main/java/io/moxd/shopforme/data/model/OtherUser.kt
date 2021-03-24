package io.moxd.shopforme.data.model

import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.serialization.Serializable

@Serializable
class OtherUser (val id :Int , val plz :String , val City :String , val profile_pic: String , val location: LocationData ,val usertype: String , val usertype_txt : String ){
}
@Serializable
class LocationData( private  val type: String, private val  coordinates: List<Double>) : java.io.Serializable{
   fun getLatLong() : LatLng{
       return  LatLng( coordinates[0], coordinates[1])
   }
}

