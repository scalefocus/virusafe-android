package com.upnetix.applicationservice.geolocation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProximityRequest(
	@SerializedName("location")
	val location: Location,
	@SerializedName("proximities")
    val proximities: MutableList<Proximity>,
	@SerializedName("timestamp")
    val timestamp: Long
) {

    override fun toString(): String {
        return ""
    }
}

data class Proximity(
    @SerializedName("distance")
    val distance: String,
    @SerializedName("uuid")
    val uuid: String
) : Serializable {

    override fun toString(): String {
        return ""
    }
}
