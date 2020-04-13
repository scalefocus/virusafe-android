package com.upnetix.applicationservice.geolocation

data class LocationEntity(
	val lat: Double = 0.0,
	val lng: Double = 0.0,
	val timestamp: Long = 0L
) {

	override fun toString(): String {
		return ""
	}
}
