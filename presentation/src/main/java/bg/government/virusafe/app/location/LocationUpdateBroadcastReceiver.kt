package bg.government.virusafe.app.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.LocationResult
import com.upnetix.applicationservice.geolocation.LocationEntity
import com.upnetix.applicationservice.workmanager.GeoLocationWorker
import java.util.concurrent.TimeUnit

/**
 * Receiver for handling location updates.
 */
class LocationUpdateBroadcastReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == ACTION_PROCESS_UPDATES) {
			LocationResult.extractResult(intent)?.locations?.forEach { location ->
				val myLocation =
					LocationEntity(
						lat = location.latitude,
						lng = location.longitude,
						timestamp = location.time
					)
				enqueueWork(context, myLocation)
			}
		}
	}

	private fun enqueueWork(context: Context, locationEntity: LocationEntity) {
		val inputData = GeoLocationWorker.createData(locationEntity)

		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val workRequest = OneTimeWorkRequest.Builder(GeoLocationWorker::class.java)
			.setConstraints(constraints)
			.setInputData(inputData)
			.setBackoffCriteria(
				BackoffPolicy.LINEAR,
				OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
				TimeUnit.MILLISECONDS)
			.build()

		WorkManager.getInstance(context).enqueue(workRequest)
	}

	companion object {
		const val ACTION_PROCESS_UPDATES =
			"bg.government.virusafe.app.location.action.PROCESS_UPDATES"
	}
}
