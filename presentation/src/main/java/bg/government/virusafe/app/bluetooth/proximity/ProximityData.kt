package bg.government.virusafe.app.bluetooth.proximity

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.upnetix.applicationservice.geolocation.Proximity
import java.io.File
import kotlin.math.pow

object ProximityData {

	const val FILE_NAME = "bluetooth_signals.txt"

	@Synchronized
	fun getSavedProximities(context: Context): MutableList<Proximity> {
		var proximitiesList = mutableListOf<Proximity>()

		try {
			val file = File(context.cacheDir, FILE_NAME)
			if (file.exists()) {
				val proximities = file.readText()
				if (proximities.isNotBlank()) {
					proximitiesList = Gson().fromJson(
						proximities,
						object : TypeToken<MutableList<Proximity?>?>() {}.type
					)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return proximitiesList
	}

	@Synchronized
	fun saveProximities(context: Context, proximitiesList: MutableList<Proximity>) {
		val f = File(context.cacheDir,FILE_NAME)
		val proximities = Gson().toJson(proximitiesList)
		f.writeText(proximities)
	}

	fun calculateDistance(rssi: Int): Double {
		// !!! hard coded power value. Usually ranges between -59 to -65
		val txPower: Int = -59
		if (rssi == 0) {
			return -1.0 // Unknown
		}
		val ratio: Double = rssi * 1.0 / txPower

		return if (ratio < 1.0) {
			ratio.pow(10)
		} else {
			(0.89976) * ratio.pow(7.7095) + 0.111
		}
	}
}