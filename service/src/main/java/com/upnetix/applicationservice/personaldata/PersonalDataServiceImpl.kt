package com.upnetix.applicationservice.personaldata

import android.content.Context
import com.upnetix.applicationservice.base.BaseService
import com.upnetix.applicationservice.base.ResponseWrapper
import javax.inject.Inject

class PersonalDataServiceImpl @Inject constructor(
	private val api: IPersonalDataApi,
	context: Context
) : BaseService(context), IPersonalDataService {

	override suspend fun deletePersonalData(): ResponseWrapper<Unit> = executeRetrofitCall {
		api.deletePersonalData()
	}
}
