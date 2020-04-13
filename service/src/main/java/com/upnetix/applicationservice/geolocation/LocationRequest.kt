package com.upnetix.applicationservice.geolocation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationRequest(
	@SerializedName("location")
	val location: Location,
	@SerializedName("timestamp")
	val timestamp: Long
) {

	override fun toString(): String {
		return ""
	}
}

data class Location(
	@SerializedName("lat")
	val lat: Double,
	@SerializedName("lng")
	val lng: Double
) : Serializable {

	override fun toString(): String {
		return ""
	}
}
