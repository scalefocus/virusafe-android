package com.upnetix.applicationservice.localization

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.imperiamobile.localizationmodule.Configuration
import com.imperiamobile.localizationmodule.ILiveFlexLocale
import com.imperiamobile.localizationmodule.LocaleHelper
import com.imperiamobile.localizationmodule.Localizer
import com.imperiamobile.localizationmodule.model.AvailableLanguage
import com.upnetix.applicationservice.BuildConfig
import com.upnetix.service.sharedprefs.ISharedPrefsService
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by stoyan.yanev on 23.2.2018 г..
 */
internal class LocalizationServiceImpl(
	private val context: Context,
	private val sharedPrefs: ISharedPrefsService
) : ILocalizationService {

	companion object {

		private val TAG = LocalizationServiceImpl::class.java.simpleName
		private const val LANGUAGE_KEY = "last_selected_language"
	}

	private val availableLanguagesList: MutableList<AvailableLanguage> = mutableListOf()

	init {
		var savedLocale = sharedPrefs.readStringFromSharedPrefs(LANGUAGE_KEY)
		if (savedLocale.isBlank()) {
			savedLocale = BuildConfig.DEFAULT_LANGUAGE
			sharedPrefs.writeStringToSharedPrefs(LANGUAGE_KEY, savedLocale)
		}
		val locale = LocaleHelper.convertToJavaLocale(savedLocale)

		val configuration = Configuration(
			BuildConfig.FLEX_BASE_URL,
			BuildConfig.FLEX_SECRET,
			BuildConfig.FLEX_DOMAINS
		)
		val instance = Localizer.getInstance()

		instance.initialize(
			context,
			{ success ->
				Log.i(
					TAG,
					if (success) "Flex initialized." else "Flex fail to initialize."
				)
			},
			locale,
			configuration
		)
	}

	override fun liveLocale(): LiveData<ILiveFlexLocale> = Localizer.getInstance().liveFlexLocale

	override fun requestAvailableLocales(callback: (Map<String, Locale>) -> Unit) {
		Localizer.getInstance().getAvailableLanguages { list ->

			if (list != null) {
				availableLanguagesList.clear()
				availableLanguagesList.addAll(list)
			}

			val mapCapacity = list?.size ?: 0
			val localesMap = HashMap<String, Locale>(mapCapacity)

			list?.forEach {
				val locale = LocaleHelper.convertToJavaLocale(it.code) ?: Locale(it.code)
				localesMap[it.name] = locale
			}

			callback(localesMap)
		}
	}

	override fun changeLocale(locale: Locale, listener: ((success: Boolean) -> Unit)?) {

		Localizer.getInstance().changeLocale(locale, context) { success, newLocale ->
			//find the id for the provided locale
			val language =
				availableLanguagesList.find { it.code.contains(newLocale.language, true) }
			val languageId =
				if (language != null) language.code else Localizer.getInstance().defaultLocaleIdentifier

			//save the provided locale
			sharedPrefs.writeStringToSharedPrefs(LANGUAGE_KEY, languageId)
			listener?.invoke(success)
		}
	}

	override fun getString(key: String): String {
		val localizedString = Localizer.getInstance().getString(key)?.trim()
		return if (!localizedString.isNullOrBlank()) {
			localizedString
		} else {
			key
		}
	}

	override fun getCurrentLocale(): Locale? =
		LocaleHelper.convertToJavaLocale(sharedPrefs.readStringFromSharedPrefs(LANGUAGE_KEY))
}
