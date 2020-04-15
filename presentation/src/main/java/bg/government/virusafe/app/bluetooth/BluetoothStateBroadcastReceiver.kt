package bg.government.virusafe.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import bg.government.virusafe.mvvm.application.HealthcareApp

/**
 * Receiver for handling location updates.
 */
class BluetoothStateBroadcastReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		val action = intent.action

		if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
			val state = intent.getIntExtra(
				BluetoothAdapter.EXTRA_STATE,
				BluetoothAdapter.ERROR
			)

			if (state == BluetoothAdapter.STATE_ON) {
				(context.applicationContext as HealthcareApp).startBluetoothLeExchange()
			}
		}
	}
}
