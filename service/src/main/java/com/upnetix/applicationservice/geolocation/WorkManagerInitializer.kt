package com.upnetix.applicationservice.geolocation

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import javax.inject.Inject

class WorkManagerInitializer @Inject constructor(
	private val workersFactory: WorkerFactory
) {

	fun init(context: Context) {
		val configuration = Configuration.Builder()
			.setWorkerFactory(workersFactory)
			.build()

		WorkManager.initialize(context.applicationContext, configuration)
	}
}
