package com.upnetix.applicationservice.pushtoken

import android.content.Context
import com.upnetix.applicationservice.base.BaseService
import com.upnetix.applicationservice.base.ResponseWrapper
import javax.inject.Inject

class PushTokenServiceImpl @Inject constructor(
	ctx: Context,
	private val api: IPushTokenApi
) : BaseService(ctx), IPushTokenService {

	override suspend fun sendPushToken(pushToken: String): ResponseWrapper<Unit> =
		executeRetrofitCall { api.sendToken(PushToken(pushToken)) }
}
