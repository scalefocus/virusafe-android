package com.upnetix.applicationservice.personaldata

import com.upnetix.applicationservice.base.ResponseWrapper

interface IPersonalDataService {

	suspend fun deletePersonalData(): ResponseWrapper<Unit>
}
