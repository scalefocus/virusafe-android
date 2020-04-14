package com.upnetix.applicationservice.base

import android.util.Log
import com.upnetix.applicationservice.registration.IRegistrationService
import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(private val serviceWrapper: Lazy<IRegistrationService>) : Authenticator {
	override fun authenticate(route: Route?, response: Response): Request? {

		return response.request().newBuilder().build()
	}
}
