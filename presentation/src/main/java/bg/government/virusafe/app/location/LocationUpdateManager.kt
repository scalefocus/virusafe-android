package bg.government.virusafe.app.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import bg.government.virusafe.app.utils.hasPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Manages all location related tasks for the app.
 */
class LocationUpdateManager private constructor(private val context: Context) {

	private var locationRequestInterval = 2L

	private val fusedLocationClient: FusedLocationProviderClient by lazy {
		LocationServices.getFusedLocationProviderClient(context)
	}

	private val locationUpdatePendingIntent: PendingIntent by lazy {
		val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
		intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
		PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
	}

	//This value is correct only while the app is live.
	val isTrackingLocation: MutableLiveData<Boolean> by lazy {
		MutableLiveData<Boolean>(false)
	}

	val locationRequest: LocationRequest by lazy {
		LocationRequest()
			.setInterval(TimeUnit.MINUTES.toMillis(locationRequestInterval))
			.setFastestInterval(TimeUnit.MINUTES.toMillis(locationRequestInterval - 1))
			.setMaxWaitTime(TimeUnit.MINUTES.toMillis(locationRequestInterval + 1))
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
	}

	fun getLastKnownLocation(
		owner: LifecycleOwner? = null,
		listener: suspend (location: Location?) -> Unit
	) {
		fusedLocationClient.lastLocation.addOnCompleteListener {
			if(owner != null) {
				owner.lifecycleScope?.launch {
					listener(if (it.isSuccessful) it.result else null)
				}
			} else {
				GlobalScope.launch {
					listener(if (it.isSuccessful) it.result else null)
				}
			}
		}
	}

	fun startLocationUpdates() {

		if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

		try {
			isTrackingLocation.value = true
			fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
		} catch (e: SecurityException) {
			isTrackingLocation.value = false
			e.printStackTrace()
		}
	}

	fun stopLocationUpdates() {
		isTrackingLocation.value = false
		fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
	}

	fun setLocationRequestInterval(locationRequestInterval: Long) {
		this.locationRequestInterval = locationRequestInterval
	}

	companion object {
		@Volatile
		private var INSTANCE: LocationUpdateManager? = null

		fun getInstance(context: Context): LocationUpdateManager {

			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: LocationUpdateManager(context).also { INSTANCE = it }
			}
		}
	}
}
