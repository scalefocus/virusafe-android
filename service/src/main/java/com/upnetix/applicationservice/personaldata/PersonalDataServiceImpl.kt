package com.upnetix.applicationservice.personaldata

import android.content.Context
import com.upnetix.applicationservice.base.BaseService
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.USE_PERSONAL_DATA_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import javax.inject.Inject

class PersonalDataServiceImpl @Inject constructor(
	private val api: IPersonalDataApi,
	private val sharedPrefs: ISharedPrefsService,
	context: Context
) : BaseService(context), IPersonalDataService {

	override suspend fun deletePersonalData(): ResponseWrapper<Unit> {
		val response = executeRetrofitCall {
			api.deletePersonalData()
		}

		if (response is ResponseWrapper.Success) {
			sharedPrefs.writeStringToSharedPrefs(USE_PERSONAL_DATA_KEY, false.toString())
		}

		return response
	}
}
