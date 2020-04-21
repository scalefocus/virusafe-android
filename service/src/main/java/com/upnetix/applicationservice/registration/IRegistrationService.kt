package com.upnetix.applicationservice.registration

import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.model.PersonalData
import com.upnetix.applicationservice.registration.model.TokenResponse

interface IRegistrationService {

	suspend fun requestPin(phoneNumber: String): ResponseWrapper<Unit>

	suspend fun verifyPin(phoneNumber: String, pin: String): ResponseWrapper<TokenResponse>

	suspend fun getPersonalData(): ResponseWrapper<PersonalData>

	suspend fun sendPersonalData(personalData: PersonalData): ResponseWrapper<Unit>

	suspend fun refreshToken(): ResponseWrapper<TokenResponse>
}
