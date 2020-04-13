package com.upnetix.applicationservice.registration

import com.upnetix.applicationservice.registration.model.PersonalData
import com.upnetix.applicationservice.registration.model.PinRequest
import com.upnetix.applicationservice.registration.model.TokenRequest
import com.upnetix.applicationservice.registration.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IRegistrationApi {

	@POST("pin")
	suspend fun getPin(@Body request: PinRequest)

	@POST("token")
	suspend fun getToken(@Body request: TokenRequest): TokenResponse

	@GET("personalinfo")
	suspend fun getPersonalData(): PersonalData

	@POST("personalinfo")
	suspend fun postPersonalData(@Body request: PersonalData)
}
