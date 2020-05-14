package com.upnetix.applicationservice

import com.upnetix.applicationservice.personaldata.IPersonalDataApi
import com.upnetix.applicationservice.pushtoken.IPushTokenApi
import com.upnetix.applicationservice.registration.IRegistrationApi
import com.upnetix.applicationservice.selfcheck.ISelfCheckApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

/**
 * @author stoyan.yanev
 */
@Module
object RetrofitApiModule {

	@Provides
	@JvmStatic
	fun provideRegistrationApi(retrofit: Retrofit): IRegistrationApi =
		retrofit.create(IRegistrationApi::class.java)

	@Provides
	@JvmStatic
	fun provideSelfCheckApi(retrofit: Retrofit): ISelfCheckApi =
		retrofit.create(ISelfCheckApi::class.java)

	@Provides
	@JvmStatic
	fun providePushTokenApi(retrofit: Retrofit): IPushTokenApi =
		retrofit.create(IPushTokenApi::class.java)

	@Provides
	@JvmStatic
	fun providePersonalDataApi(retrofit: Retrofit): IPersonalDataApi =
		retrofit.create(IPersonalDataApi::class.java)
}
