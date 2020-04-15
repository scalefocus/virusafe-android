package bg.government.virusafe.app.bluetooth.proximity

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import bg.government.virusafe.app.location.LocationUpdateManager
import com.upnetix.applicationservice.geolocation.Location
import com.upnetix.applicationservice.geolocation.Proximity
import com.upnetix.applicationservice.workmanager.GeoLocationWorker
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

class ProximityWorker(
	val context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result = coroutineScope {
		try {
			val proximitiesList = ProximityData.getSavedProximities(context)
			if(proximitiesList.isNotEmpty()) {
				enqueueWork(ProximityData.getSavedProximities(context))
				// clear the file
				ProximityData.saveProximities(context, mutableListOf())
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		Result.success()
	}

	private fun enqueueWork(proximitiesList: MutableList<Proximity>) {
		LocationUpdateManager.getInstance(context)
			.getLastKnownLocation {
				it?.let {
					val inputData = GeoLocationWorker.createProximityData(
						proximitiesList, Location(it.latitude, it.longitude)
					)
					val constraints = Constraints.Builder()
						.setRequiredNetworkType(NetworkType.CONNECTED)
						.build()

					val workRequest = OneTimeWorkRequest.Builder(GeoLocationWorker::class.java)
						.setConstraints(constraints)
						.setInputData(inputData)
						.build()

					WorkManager.getInstance(context).enqueue(workRequest)
				}
			}
	}
}