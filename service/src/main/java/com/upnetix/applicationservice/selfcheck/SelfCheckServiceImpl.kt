package com.upnetix.applicationservice.selfcheck

import android.content.Context
import com.upnetix.applicationservice.base.BaseService
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.selfcheck.model.Question
import com.upnetix.applicationservice.selfcheck.model.QuestionnaireRequest
import javax.inject.Inject

class SelfCheckServiceImpl @Inject constructor(
	private val api: ISelfCheckApi,
	ctx: Context
) : BaseService(ctx), ISelfCheckService {

	override suspend fun getQuestions(): ResponseWrapper<List<Question>> = executeRetrofitCall {
		api.getQuestionnaire()
	}

	override suspend fun sendQuestionnaire(questionnaireRequest: QuestionnaireRequest): ResponseWrapper<Unit> =
		executeRetrofitCall { api.sendQuestionnaire(questionnaireRequest) }
}
