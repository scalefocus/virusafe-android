package com.upnetix.applicationservice.personaldata

import retrofit2.http.DELETE

interface IPersonalDataApi {

	@DELETE("personalinfo")
	suspend fun deletePersonalData()

}
