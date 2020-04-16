package com.upnetix.applicationservice.base

import com.upnetix.applicationservice.base.BaseService.Companion.INVALID_TOKEN
import com.upnetix.applicationservice.base.HeaderInterceptor.Companion.AUTHORIZATION_KEY
import com.upnetix.applicationservice.base.HeaderInterceptor.Companion.BEARER_KEY
import com.upnetix.applicationservice.registration.IRegistrationService
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(private val serviceWrapper: Lazy<IRegistrationService>) : Authenticator {

	private var retryCount = 0;

	override fun authenticate(route: Route?, response: Response): Request? = runBlocking {

		if (retryCount > 1) {
			throw AuthProtocolException(INVALID_TOKEN)
		}
		retryCount++

		try {
			when (val tokenResponse = serviceWrapper.get()?.refreshToken()) {
				is ResponseWrapper.Success -> response.request().newBuilder().header(
					AUTHORIZATION_KEY,
					"$BEARER_KEY ${tokenResponse.response.accessToken}"
				).build()
				is ResponseWrapper.Error -> throw AuthProtocolException(tokenResponse.code)
				else -> null
			}
		} finally {
			retryCount = 0
		}
	}
}
