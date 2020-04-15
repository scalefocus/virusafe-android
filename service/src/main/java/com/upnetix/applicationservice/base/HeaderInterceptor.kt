package com.upnetix.applicationservice.base

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.imperiamobile.localizationmodule.LocaleHelper
import com.upnetix.applicationservice.BuildConfig
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.BLUETOOTH_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.TOKEN_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.Charset
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
		var bluetoothId = sharedPrefs.readStringFromSharedPrefs(BLUETOOTH_KEY)
		if (token.isNotBlank()) {
			requestBuilder.addHeader(AUTHORIZATION_KEY, "$BEARER_KEY $token")
			if (bluetoothId.isEmpty()) {
				sharedPrefs.writeStringToSharedPrefs(BLUETOOTH_KEY, getBluetoothId(token))
			}
		}

		proceed(requestBuilder.build())
	}

	private fun getBluetoothId(JWTEncoded: String): String {
		return try {
			val split = JWTEncoded.split(".").toTypedArray()

			val payload = Gson().fromJson(getJson(split[1]), JsonObject::class.java) as JsonObject

			payload.get(USER_GUID).asString
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}

	private fun getJson(strEncoded: String): String {
		val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
		return String(decodedBytes, Charset.forName("UTF-8"))
	}

	companion object {
		private const val USER_GUID = "userGuid"
		private const val CLIENT_ID_KEY = "clientId"
		private const val LANGUAGE_KEY = "language"
		private const val AUTHORIZATION_KEY = "Authorization"
		private const val BEARER_KEY = "Bearer"
	}
}
