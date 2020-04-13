package bg.government.virusafe.mvvm.viewmodel

import androidx.lifecycle.LiveData
import com.imperiamobile.localizationmodule.ILiveFlexLocale
import com.upnetix.applicationservice.localization.ILocalizationService
import com.upnetix.presentation.viewmodel.BaseViewModel
import java.util.*
import javax.inject.Inject

/**
 * Abstract class for creating view models.
 *
 * @author stoyan.yanev
 */
abstract class AbstractViewModel : BaseViewModel() {

	@Inject
	protected lateinit var localizationService: ILocalizationService

	fun liveLocale(): LiveData<ILiveFlexLocale> = localizationService.liveLocale()

	/**
	 * Use to change the current locale.
	 */
	fun changeLocale(locale: Locale, listener: ((success: Boolean) -> Unit)? = null) {
		localizationService.changeLocale(locale, listener);
	}

	/**
	 * Use to get the available locales.
	 */
	fun requestAvailableLocales(callback: (Map<String, Locale>) -> Unit) {
		localizationService.requestAvailableLocales(callback)
	}

	fun localizeString(key: String) = localizationService.getString(key)

	fun getCurrentLocale() = localizationService.getCurrentLocale()
}
