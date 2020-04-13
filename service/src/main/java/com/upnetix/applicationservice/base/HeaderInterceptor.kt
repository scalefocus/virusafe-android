package com.upnetix.applicationservice.base

import com.imperiamobile.localizationmodule.LocaleHelper
import com.upnetix.applicationservice.BuildConfig
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.TOKEN_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
	val sharedPrefs: ISharedPrefsService
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response = chain.run {
		val requestBuilder = request()
			.newBuilder()
			.addHeader(CLIENT_ID_KEY, BuildConfig.CLIENT_ID)

		var languageValue = sharedPrefs.readStringFromSharedPrefs(LANGUAGE_KEY)
		if (languageValue.isBlank()) {
			languageValue = BuildConfig.DEFAULT_LANGUAGE
		}
		val locale = LocaleHelper.convertToJavaLocale(languageValue)
		locale?.let {
			requestBuilder.addHeader(LANGUAGE_KEY, it.language)
		}

		val token = sharedPrefs.readStringFromSharedPrefs(TOKEN_KEY)
		if (token.isNotBlank()) {
			requestBuilder.addHeader(AUTHORIZATION_KEY, "$BEARER_KEY $token")
		}

		proceed(requestBuilder.build())
	}

	companion object {
		private const val CLIENT_ID_KEY = "clientId"
		private const val LANGUAGE_KEY = "language"
		private const val AUTHORIZATION_KEY = "Authorization"
		private const val BEARER_KEY = "Bearer"
	}
}
