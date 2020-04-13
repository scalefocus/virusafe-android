package bg.government.virusafe.mvvm.di

import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService
import bg.government.virusafe.mvvm.application.HealthcareApp
import com.upnetix.applicationservice.ServiceModule
import com.upnetix.presentation.di.component.IBaseAppComponent
import com.upnetix.service.sharedprefs.ISharedPrefsService
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

/**
 * The app component used for DI.
 * This component should be used as dependency in every other component in the app.
 * When the component is set as dependency it is necessary to explicitly provide methods
 * for injecting objects from the modules that are used.
 * This is needed so other components can reference the provided dependencies from the modules.
 *
 * @author stoyan.yanev
 */
@Singleton
@Component(
	dependencies = [IBaseAppComponent::class],
	modules = [AppModule::class, ServiceModule::class]
)
interface IAppComponent : IBaseAppComponent, AndroidInjector<HealthcareApp> {

	fun getSharedPrefsService(): ISharedPrefsService

	fun inject(service: FirebaseCloudMessagingService)
}
