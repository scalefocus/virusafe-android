package bg.government.virusafe.mvvm.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import bg.government.virusafe.mvvm.di.DaggerIAppComponent
import bg.government.virusafe.mvvm.di.IAppComponent
import com.jakewharton.threetenabp.AndroidThreeTen
import com.upnetix.applicationservice.ServiceModule
import com.upnetix.applicationservice.geolocation.WorkManagerInitializer
import com.upnetix.presentation.BaseApplication
import com.upnetix.presentation.di.component.IBaseAppComponent
import javax.inject.Inject

/**
 * @author stoyan.yanev
 */
class HealthcareApp : BaseApplication<IAppComponent>(), Application.ActivityLifecycleCallbacks {

	@Inject
	lateinit var workManagerInitializer: WorkManagerInitializer

	private var mCurrentActivity: Activity? = null

	private val serviceModule: ServiceModule = ServiceModule(this)

	override fun initAppComponent(baseAppComponent: IBaseAppComponent): IAppComponent {

		val build = DaggerIAppComponent.builder()
			.serviceModule(serviceModule)
			.iBaseAppComponent(baseAppComponent)
			.build()

		build.inject(this)
		return build
	}

	override fun onCreate() {
		super.onCreate()
		AndroidThreeTen.init(this)
		workManagerInitializer.init(this)
		this.registerActivityLifecycleCallbacks(this);
	}

	fun setEndpoint(endpoint: String) {
		if (endpoint.isBlank() || endpoint.isEmpty()) {
			return
		}
		serviceModule.setEndpoint(endpoint)
	}

	fun getCurrentActivity(): Activity? {
		return mCurrentActivity
	}

	override fun onActivityStarted(activity: Activity) {
		mCurrentActivity = activity
	}

	override fun onActivityResumed(activity: Activity) {}

	override fun onActivityPaused(activity: Activity) {}

	override fun onActivityDestroyed(activity: Activity) {}

	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

	override fun onActivityStopped(activity: Activity) {}

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}
