package bg.government.virusafe.app.home

import androidx.databinding.ObservableField
import bg.government.virusafe.app.splash.SplashActivity.Companion.IS_STATISTICS_BTN_VISIBLE_KEY
import bg.government.virusafe.app.splash.SplashActivity.Companion.STATISTICS_URL_KEY
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.service.sharedprefs.ISharedPrefsService
import javax.inject.Inject

class HomeViewModel @Inject constructor(var sharedPrefsService: ISharedPrefsService) : AbstractViewModel() {

	var isStatisticsBtnVisible: ObservableField<Boolean> = ObservableField()

	init {
		setStatisticsBtnVisibility()
	}

	private fun setStatisticsBtnVisibility() {
		val isStatisticsBtnVisibleStr =
			sharedPrefsService.readStringFromSharedPrefs(IS_STATISTICS_BTN_VISIBLE_KEY)
		val statisticsContentUrl =
			sharedPrefsService.readStringFromSharedPrefs(STATISTICS_URL_KEY)
		if (isStatisticsBtnVisibleStr.isBlank() || statisticsContentUrl.isBlank()) {
			isStatisticsBtnVisible.set(false)
		} else {
			try {
				isStatisticsBtnVisible.set(isStatisticsBtnVisibleStr.toBoolean())
			} catch (e: Exception) {
				isStatisticsBtnVisible.set(false)
			}
		}
	}
}
