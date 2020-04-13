package com.upnetix.applicationservice.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.upnetix.applicationservice.ServiceModule
import com.upnetix.service.retrofit.ApiConverterFactory
import com.upnetix.service.retrofit.ApiInterceptors
import com.upnetix.service.retrofit.ApiLogging
import com.upnetix.service.retrofit.ApiSSLData
import com.upnetix.service.retrofit.SSLData
import com.upnetix.service.sharedprefs.ISharedPrefsService
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A factory responsible for creation and dependency
 * injection of all workers in the project
 */
@Singleton
class WorkersFactory @Inject constructor(
	private val module: ServiceModule,
	@ApiSSLData
	private val sslData: SSLData?,
	@ApiLogging
	private val logLevel: HttpLoggingInterceptor.Level,
	@ApiInterceptors
	private val interceptors: Array<Interceptor>?,
	@ApiConverterFactory
	private val converterFactory: Converter.Factory,
	private val sharedPrefs: ISharedPrefsService
) : WorkerFactory() {

	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters
	): ListenableWorker? = when (workerClassName) {
		GeoLocationWorker::class.java.name -> GeoLocationWorker(
			appContext,
			workerParameters,
			module,
			interceptors,
			sslData,
			logLevel,
			converterFactory,
			sharedPrefs
		)
		else -> null
	}
}
