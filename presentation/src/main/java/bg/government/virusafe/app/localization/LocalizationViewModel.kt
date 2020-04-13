package bg.government.virusafe.app.localization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import bg.government.virusafe.app.utils.COMMON_KEY
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import java.util.*
import javax.inject.Inject

class LocalizationViewModel @Inject constructor() : AbstractViewModel() {

	private val _localeData = MutableLiveData<List<AppLocale>>()
	val localeData: LiveData<List<AppLocale>> = _localeData

	private var localeList: List<AppLocale>? = null

	fun requestLocales() {
		requestAvailableLocales {
			localeList = it.map { entry ->
				AppLocale(
					localeStr = localizeString(COMMON_KEY + entry.value.language),
					locale = entry.value,
					isSelected = liveLocale().value?.locale == entry.value
				)
			}
			_localeData.postValue(localeList)
		}
	}

	fun setNewLocale(appLocale: AppLocale, listener: ((success: Boolean) -> Unit)? = null) {
		localeList?.forEach { it.isSelected = it.locale == appLocale.locale }
		_localeData.postValue(localeList)
		changeLocale(appLocale.locale, listener)
	}

	data class AppLocale(val localeStr: String, val locale: Locale, var isSelected: Boolean)
}
