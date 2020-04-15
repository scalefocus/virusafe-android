package bg.government.virusafe.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import androidx.preference.PreferenceManager
import bg.government.virusafe.app.bluetooth.beacon.BeaconId
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdAgent
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdLocal
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdRemote
import bg.government.virusafe.app.bluetooth.proximity.ProximityData
import com.upnetix.applicationservice.geolocation.Proximity
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.BLUETOOTH_KEY
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class BeaconIdManager(val context: Context) : BeaconIdAgent {
	private val handler = Handler()
	private fun createNewBeaconId(): BeaconIdLocal {
		val newBeaconId = BeaconIdLocal(
			BeaconId(
				UuidAdapter.getBytesFromUUID(UUID.fromString(getUUID(context))),
				ProteGoManufacturerDataVersion
			),
			Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3)),
			BluetoothAdapter.getDefaultAdapter().address ?: ""
		)
		handler.postDelayed(
			{ currentBeaconId = createNewBeaconId() },
			newBeaconId.expirationDate.time - System.currentTimeMillis()
		)
		return newBeaconId
	}

	private var listeners = setOf<BeaconIdAgent.Listener>()

	private var currentBeaconId = createNewBeaconId()
		set(value) {
			listeners.forEach { it.useBeaconId(value) }
			field = value
		}

	override fun getBeaconId(): BeaconIdLocal? = currentBeaconId

	override fun registerListener(listener: BeaconIdAgent.Listener) {
		listeners = listeners + listener
		listener.useBeaconId(currentBeaconId)
	}

	override fun unregisterListener(listener: BeaconIdAgent.Listener) {
		listeners = listeners - listener
	}

	@Synchronized
	override fun synchronizedBeaconId(beaconIdRemote: BeaconIdRemote) {
		val distance = ProximityData.calculateDistance(beaconIdRemote.rssi ?: 0)

		val logBeacon = TextUtils.concat(
			"Beacon: ", UuidAdapter.getUUIDFromBytes(beaconIdRemote.beaconId.byteArray).toString(),
			" distance: ", String.format("%.1f", distance),
			" rssi: ", beaconIdRemote.rssi.toString(),
			" Mac: ", beaconIdRemote.bluetoothMacAddress, " ",
			"time: ", Calendar.getInstance().time.toString(),  "\n").toString()

		Timber.i(logBeacon)

		val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
		val savedBeacons = sharedPrefs.getString("beacons", "")
		val editor = sharedPrefs.edit()
		editor.putString("beacons", "$savedBeacons \n $logBeacon")
		editor.apply()

		if (beaconIdRemote.rssi == null || distance < 0)
			return

		val proximitiesList = ProximityData.getSavedProximities(context)

		proximitiesList.add(
			Proximity(
				String.format(
					Locale.US,
					"%.1f",
					distance
				), UuidAdapter.getUUIDFromBytes(beaconIdRemote.beaconId.byteArray).toString()
			)
		)

		ProximityData.saveProximities(context, proximitiesList)
	}

	private fun getUUID(context: Context): String =
		PreferenceManager.getDefaultSharedPreferences(context).getString(BLUETOOTH_KEY, "") ?: ""
}