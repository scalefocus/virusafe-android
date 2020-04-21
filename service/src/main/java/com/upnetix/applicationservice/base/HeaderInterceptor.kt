package com.upnetix.applicationservice.base

import com.imperiamobile.localizationmodule.LocaleHelper
import com.upnetix.applicationservice.BuildConfig
import com.upnetix.applicationservice.encryption.IEncryptionService
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.NEW_ACCESS_TOKEN_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.OLD_TOKEN_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
	val sharedPrefs: ISharedPrefsService,
	val encryptionService: IEncryptionService
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

		var token: String? = sharedPrefs.readStringFromSharedPrefs(NEW_ACCESS_TOKEN_KEY)
		if (token.isNullOrBlank()) {
			val encryptedValue = sharedPrefs.readStringFromSharedPrefs(OLD_TOKEN_KEY)
			if (encryptedValue.isNotBlank()) {
				token = encryptionService.decryptValue(encryptedValue)
				sharedPrefs.clearValue(OLD_TOKEN_KEY)
			}
		}
		token?.let {
			if (it.isBlank().not()) {
				sharedPrefs.writeStringToSharedPrefs(NEW_ACCESS_TOKEN_KEY, it)
				requestBuilder.addHeader(AUTHORIZATION_KEY, "$BEARER_KEY $it")
			}
		}

		proceed(requestBuilder.build())
	}

	companion object {
		private const val CLIENT_ID_KEY = "clientId"
		private const val LANGUAGE_KEY = "language"

		const val AUTHORIZATION_KEY = "Authorization"
		const val BEARER_KEY = "Bearer"
	}
}
