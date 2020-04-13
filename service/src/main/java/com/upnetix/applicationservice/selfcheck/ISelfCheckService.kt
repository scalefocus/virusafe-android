package com.upnetix.applicationservice.selfcheck

import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.selfcheck.model.Question
import com.upnetix.applicationservice.selfcheck.model.QuestionnaireRequest

interface ISelfCheckService {

	suspend fun getQuestions(): ResponseWrapper<List<Question>>

	suspend fun sendQuestionnaire(questionnaireRequest: QuestionnaireRequest): ResponseWrapper<Unit>
}
