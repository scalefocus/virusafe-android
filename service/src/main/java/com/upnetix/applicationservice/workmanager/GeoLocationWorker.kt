package com.upnetix.applicationservice.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.upnetix.applicationservice.ServiceModule
import com.upnetix.applicationservice.geolocation.IGeoLocationApi
import com.upnetix.applicationservice.geolocation.Location
import com.upnetix.applicationservice.geolocation.LocationEntity
import com.upnetix.applicationservice.geolocation.LocationRequest
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.USE_PERSONAL_DATA_KEY
import com.upnetix.service.retrofit.RetrofitModule
import com.upnetix.service.retrofit.SSLData
import com.upnetix.service.sharedprefs.ISharedPrefsService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.HttpException

class GeoLocationWorker(
	context: Context,
	params: WorkerParameters,
	private val module: ServiceModule,
	private val interceptors: Array<Interceptor>?,
	private val sslData: SSLData?,
	private val authenticator: Authenticator?,
	private val logLevel: HttpLoggingInterceptor.Level,
	private val converterFactory: Converter.Factory,
	private val sharedPrefs: ISharedPrefsService
) : CoroutineWorker(context, params) {

	companion object {
		private const val LONGITUDE = "val1"
		private const val LATITUDE = "val2"
		private const val DATE = "val3"
		private const val DELIMITER = "-"
		private const val VALUE_KEY = "value_key"

		/**
		 * Transmit the arguments for this work
		 * @param entity [LocationEntity] received location
		 */
		fun createData(entity: LocationEntity) =
			Data.Builder()
				.putDouble(LONGITUDE, entity.lng)
				.putDouble(LATITUDE, entity.lat)
				.putLong(DATE, entity.timestamp)
				.build()
	}

	override suspend fun doWork(): Result = coroutineScope {
		// Disable tracking if the user has denied consent
		if (!sharedPrefs.readStringFromSharedPrefs(USE_PERSONAL_DATA_KEY).toBoolean()) return@coroutineScope Result.failure()

		val longitude = inputData.getDouble(LONGITUDE, 0.0)
		val latitude = inputData.getDouble(LATITUDE, 0.0)
		val date = inputData.getLong(DATE, 0)
		val coordinatesMatch = coordinatesMatch(latitude, longitude)
		if (coordinatesMatch) {
			return@coroutineScope Result.success()
		}
		val deferredJob = async {
			val request = LocationRequest(Location(latitude, longitude), date)
			try {
				val retrofit =
					RetrofitModule.provideRetrofit(
						module.endPoint,
						interceptors,
						converterFactory,
						sslData,
						logLevel,
						authenticator
					)
				val api = retrofit.create(IGeoLocationApi::class.java)
				api.sendLocation(request)
			} catch (ex: HttpException) {
				//do nothing
			}
		}

		deferredJob.await()
		Result.success()
	}

	private fun coordinatesMatch(latitude: Double, longitude: Double): Boolean {
		val savedValue = sharedPrefs.readStringFromSharedPrefs(VALUE_KEY)
		var coordinatesMatch = false
		if (savedValue.isNotBlank()) {
			savedValue.split(DELIMITER)?.let {
				if (it.size != 2) return@let
				coordinatesMatch = it[0].toDouble() == latitude && it[1].toDouble() == longitude
			}
		}
		saveCoordinates(latitude, longitude)
		return coordinatesMatch
	}

	private fun saveCoordinates(latitude: Double, longitude: Double) {
		val latLngValue = "$latitude$DELIMITER$longitude"
		sharedPrefs.writeStringToSharedPrefs(VALUE_KEY, latLngValue)
	}
}
