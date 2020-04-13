package com.upnetix.applicationservice.geolocation

import retrofit2.http.Body
import retrofit2.http.POST

interface IGeoLocationApi {

	@POST("location/gps")
	suspend fun sendLocation(@Body request: LocationRequest)
}
