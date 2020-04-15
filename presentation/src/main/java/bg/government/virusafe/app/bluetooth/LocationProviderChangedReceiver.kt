package bg.government.virusafe.app.bluetooth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import bg.government.virusafe.mvvm.application.HealthcareApp

/**
 * Receiver for handling location updates.
 */
class LocationProviderChangedReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		val action = intent.action

		if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
			val locationManager =
				context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
			val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
			val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

			if (isGpsEnabled || isNetworkEnabled) {
				(context.applicationContext as HealthcareApp).startBluetoothLeExchange()
			}
		}
	}
}
