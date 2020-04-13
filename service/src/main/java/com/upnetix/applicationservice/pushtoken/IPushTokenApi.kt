package com.upnetix.applicationservice.pushtoken

import retrofit2.http.Body
import retrofit2.http.POST

interface IPushTokenApi {

	@POST("pushtoken")
	suspend fun sendToken(@Body token: PushToken)
}
