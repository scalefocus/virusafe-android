package com.upnetix.applicationservice

import android.app.Application
import android.content.Context
import androidx.work.WorkerFactory
import com.upnetix.applicationservice.base.HeaderInterceptor
import com.upnetix.applicationservice.geolocation.WorkManagerInitializer
import com.upnetix.applicationservice.localization.ILocalizationService
import com.upnetix.applicationservice.localization.LocalizationServiceImpl
import com.upnetix.applicationservice.pushtoken.IPushTokenService
import com.upnetix.applicationservice.pushtoken.PushTokenServiceImpl
import com.upnetix.applicationservice.registration.IRegistrationService
import com.upnetix.applicationservice.registration.RegistrationServiceImpl
import com.upnetix.applicationservice.selfcheck.ISelfCheckService
import com.upnetix.applicationservice.selfcheck.SelfCheckServiceImpl
import com.upnetix.applicationservice.workmanager.WorkersFactory
import com.upnetix.service.BaseServiceModule
import com.upnetix.service.retrofit.ApiConverterFactory
import com.upnetix.service.retrofit.ApiEndpoint
import com.upnetix.service.retrofit.ApiInterceptors
import com.upnetix.service.retrofit.ApiLogging
import com.upnetix.service.retrofit.ApiSSLData
import com.upnetix.service.retrofit.SSLData
import com.upnetix.service.sharedprefs.ISharedPrefsService
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author stoyan.yanev
 */
@Module(includes = [BaseServiceModule::class, RetrofitApiModule::class])
class ServiceModule(application: Application) : BaseServiceModule(application) {

	@Module
	companion object {

		/**
		 * Provide the api endpoint.
		 *
		 * @return the api url.
		 */
		@Provides
		@ApiEndpoint
		@JvmStatic
		fun provideEndpoint(module: ServiceModule): String = module.endPoint

		/**
		 * Provide [retrofit2.Retrofit] interceptors.
		 *
		 * @return the interceptors.
		 */
		@Singleton
		@Provides
		@ApiInterceptors
		@JvmStatic
		fun provideInterceptors(headerInterceptor: HeaderInterceptor): Array<Interceptor>? =
			arrayOf(headerInterceptor)

		@Singleton
		@Provides
		@JvmStatic
		fun headerInterceptor(
			sharedPrefs: ISharedPrefsService
		): Interceptor = HeaderInterceptor(sharedPrefs)

		@Singleton
		@Provides
		@ApiSSLData
		@JvmStatic
		fun provideSSLData(): SSLData? = null

		@Singleton
		@Provides
		@ApiLogging
		@JvmStatic
		fun provideLogLevel(): HttpLoggingInterceptor.Level =
			if (BuildConfig.ENABLE_HTTP_LOGGING)
				HttpLoggingInterceptor.Level.BODY
			else
				HttpLoggingInterceptor.Level.NONE

		/**
		 * Provide localization service.
		 *
		 * @param appContext         the app context
		 * @param sharedPrefsService shared preferences service
		 * @return the localization service.
		 */
		@Provides
		@Singleton
		@JvmStatic
		fun provideLocalizationService(
			appContext: Context,
			sharedPrefsService: ISharedPrefsService
		): ILocalizationService = LocalizationServiceImpl(appContext, sharedPrefsService)

		/**
		 * Provide converter factory to the [retrofit2.Retrofit] instance.
		 *
		 * @return the converter factory
		 */
		@Singleton
		@Provides
		@ApiConverterFactory
		@JvmStatic
		fun provideConverterFactory(): Converter.Factory = GsonConverterFactory.create()

		@Provides
		@Singleton
		@JvmStatic
		fun provideRegistrationService(service: RegistrationServiceImpl): IRegistrationService =
			service

		@Provides
		@Singleton
		@JvmStatic
		fun provideSelfCheckService(service: SelfCheckServiceImpl): ISelfCheckService = service

		@Provides
		@JvmStatic
		@Singleton
		fun workerFactory(factory: WorkersFactory): WorkerFactory = factory

		@Provides
		@JvmStatic
		@Singleton
		fun workManagerInitializer(factory: WorkerFactory) = WorkManagerInitializer(factory)

		@Provides
		@JvmStatic
		fun pushTokenService(pushTokenServiceImpl: PushTokenServiceImpl): IPushTokenService =
			pushTokenServiceImpl
	}

	/**
	 * Used to provide the app context to the services.
	 *
	 * @return the app context
	 */
	@Provides
	fun provideAppContext(): Context = application

	@Provides
	fun module(): ServiceModule = this

	var endPoint = BuildConfig.DEFAULT_ENDPOINT
		private set

	fun setEndpoint(endPoint: String) {
		this.endPoint = endPoint
	}
}
