package com.upnetix.applicationservice.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.upnetix.applicationservice.ServiceModule
import com.upnetix.applicationservice.geolocation.IGeoLocationApi
import com.upnetix.applicationservice.geolocation.Location
import com.upnetix.applicationservice.geolocation.LocationEntity
import com.upnetix.applicationservice.geolocation.LocationRequest
import com.upnetix.applicationservice.geolocation.Proximity
import com.upnetix.applicationservice.geolocation.ProximityRequest
import com.upnetix.service.retrofit.RetrofitModule
import com.upnetix.service.retrofit.SSLData
import com.upnetix.service.sharedprefs.ISharedPrefsService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.HttpException
import java.util.*

class GeoLocationWorker(
	context: Context,
	params: WorkerParameters,
	private val module: ServiceModule,
	private val interceptors: Array<Interceptor>?,
	private val sslData: SSLData?,
	private val logLevel: HttpLoggingInterceptor.Level,
	private val converterFactory: Converter.Factory,
	private val sharedPrefs: ISharedPrefsService
) : CoroutineWorker(context, params) {

	companion object {
		private const val LONGITUDE = "val1"
		private const val LATITUDE = "val2"
		private const val DATE = "val3"
		private const val PROXIMITIES = "val4"
		private const val IS_PROXIMITY = "val5"
		private const val DELIMITER = "-"
		private const val VALUE_KEY = "value_key"

		/**
		 * Transmit the arguments for this work
		 * @param entity [LocationEntity] received location
		 */
		fun createLocationData(entity: LocationEntity) =
			Data.Builder()
				.putDouble(LONGITUDE, entity.lng)
				.putDouble(LATITUDE, entity.lat)
				.putLong(DATE, entity.timestamp)
				.putBoolean(IS_PROXIMITY, false)
				.build()

		/**
		 * Transmit the arguments for this work
		 * @param entity [Proximity] received iBeacon
		 */
		fun createProximityData(proximitiesList: MutableList<Proximity>, location: Location): Data =
			Data.Builder()
				.putString(PROXIMITIES, Gson().toJson(proximitiesList))
				.putDouble(LONGITUDE, location.lng)
				.putDouble(LATITUDE, location.lat)
				.putBoolean(IS_PROXIMITY, true)
				.build()
	}

	override suspend fun doWork(): Result = coroutineScope {
		val isProximityRequest = inputData.getBoolean(IS_PROXIMITY, false)
		val longitude = inputData.getDouble(LONGITUDE, 0.0)
		val latitude = inputData.getDouble(LATITUDE, 0.0)
		val date = if (isProximityRequest) {
			Calendar.getInstance().timeInMillis
		} else {
			inputData.getLong(DATE, 0)
		}

		if (isProximityRequest.not()) {
			val coordinatesMatch = coordinatesMatch(latitude, longitude)
			if (coordinatesMatch) {
				return@coroutineScope Result.success()
			}
		}

		val deferredJob = async {
			try {
				val retrofit =
					RetrofitModule.provideRetrofit(
						module.endPoint,
						interceptors,
						converterFactory,
						sslData,
						logLevel
					)
				val api = retrofit.create(IGeoLocationApi::class.java)

				if (isProximityRequest) {
					val proximityEntities: MutableList<Proximity> = Gson().fromJson(
						inputData.getString(PROXIMITIES),
						object : TypeToken<MutableList<Proximity?>?>() {}.type
					)
					api.sendProximity(
						ProximityRequest(
							Location(latitude, longitude),
							proximityEntities,
							date
						)
					)
				} else {
					api.sendLocation(LocationRequest(Location(latitude, longitude), date))
				}
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
