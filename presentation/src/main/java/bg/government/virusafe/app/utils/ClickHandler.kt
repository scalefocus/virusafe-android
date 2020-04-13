package bg.government.virusafe.app.utils

import android.os.SystemClock

private const val MIN_CLICK_INTERVAL = 700

/**
 * Use this class when you want to ensure you have more time between click events
 */
class ClickHandler {

	private var lastClickTime = 0L

	/**
	 * @return true if click is possible
	 */
	fun canPerformClick(): Boolean {
		if (SystemClock.elapsedRealtime()
			- lastClickTime < MIN_CLICK_INTERVAL
		) {
			return false
		}
		lastClickTime = SystemClock.elapsedRealtime()
		return true
	}
}
