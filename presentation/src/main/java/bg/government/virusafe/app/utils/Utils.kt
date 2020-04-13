package bg.government.virusafe.app.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import com.upnetix.applicationservice.geolocation.LocationEntity

/**
 * Helper function to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

	// Background permissions didn't exit prior to Q, so it's approved by default.
	if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
		android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
	) {

		return true
	}

	return ActivityCompat.checkSelfPermission(this, permission) ==
			PackageManager.PERMISSION_GRANTED
}

/**
 * Helper function to check if the app is on the foreground.
 */
fun Context.isAppInForeground(): Boolean {
	val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
	val appProcesses = activityManager.runningAppProcesses ?: return false

	val packageName = this.packageName

	for (appProcess in appProcesses) {
		if (appProcess.importance ==
			ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
			appProcess.processName == packageName
		) {
			return true
		}
	}
	return false
}

fun Location.toLocationEntity(): LocationEntity =
	LocationEntity(
		this.latitude,
		this.longitude,
		this.time
	)

fun <T> SparseArray<T>.values(): List<T> {
	val list = ArrayList<T>()
	forEach { _, value ->
		list.add(value)
	}
	return list.toList()
}
