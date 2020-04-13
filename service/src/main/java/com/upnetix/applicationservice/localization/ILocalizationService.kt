package com.upnetix.applicationservice.localization

import androidx.lifecycle.LiveData
import com.imperiamobile.localizationmodule.ILiveFlexLocale
import java.util.*

/**
 * Service for loading strings.
 * This should be used only from the view model classes.
 *
 * Created by stoyan.yanev on 23.2.2018 г..
 */
interface ILocalizationService {

	/**
	 * Use the Flex locale object to listen for locale changes and requesting string translations.
	 *
	 * @return the Flex locale object
	 */
	fun liveLocale(): LiveData<ILiveFlexLocale>

	/**
	 * Note that this method initiate async network call. If you want to block user's interface
	 * while network call is made you should implement that yourself.
	 *
	 * @param callback callback that will be notified when languages are received and parsed.
	 * The parsed languages are inserted in a Map,
	 * where the key is representing the language name as [String] and the value is the actual [Locale] object.
	 */
	fun requestAvailableLocales(callback: (Map<String, Locale>) -> Unit)

	/**
	 * Use to change the current locale.
	 *
	 * @param locale The desired locale
	 */
	fun changeLocale(locale: Locale, listener: ((success: Boolean) -> Unit)? = null)

	fun getString(key: String): String

	fun getCurrentLocale() : Locale?
}
