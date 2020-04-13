package com.upnetix.applicationservice.pushtoken

import com.upnetix.applicationservice.base.ResponseWrapper

interface IPushTokenService {

	suspend fun sendPushToken(pushToken: String): ResponseWrapper<Unit>
}
