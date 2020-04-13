package com.upnetix.applicationservice.selfcheck

import com.upnetix.applicationservice.selfcheck.model.Question
import com.upnetix.applicationservice.selfcheck.model.QuestionnaireRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ISelfCheckApi {

	@GET("questionnaire")
	suspend fun getQuestionnaire(): List<Question>

	@POST("questionnaire")
	suspend fun sendQuestionnaire(@Body request: QuestionnaireRequest)
}
