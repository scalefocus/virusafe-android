package bg.government.virusafe.app.splash

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.core.content.pm.PackageInfoCompat
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService.Companion.KEY_LOCATION_INTERVAL_IN_MINS
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService.Companion.URL
import bg.government.virusafe.app.location.LocationUpdateManager
import bg.government.virusafe.app.utils.CONTINUE_LABEL
import bg.government.virusafe.app.utils.NEW_VERSION_LABEL
import bg.government.virusafe.app.utils.NEW_VERSION_MSG
import bg.government.virusafe.app.utils.UPDATE_LABEL
import bg.government.virusafe.databinding.ActivitySplashBinding
import bg.government.virusafe.mvvm.activity.AbstractActivity
import bg.government.virusafe.mvvm.application.HealthcareApp
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.upnetix.presentation.navigation.ACTIVITY_BUNDLE_EXTRA_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import javax.inject.Inject

class 	SplashActivity : AbstractActivity<ActivitySplashBinding, EmptyViewModel>() {
	private val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
	private lateinit var firebaseDefaultMap: HashMap<String, Any>
	private var isForceUpdateDialogShown = false

	@Inject
	lateinit var sharedPrefsService: ISharedPrefsService

	override fun getLayoutResId(): Int = R.layout.activity_splash

	override fun getViewModelClass(): Class<EmptyViewModel> = EmptyViewModel::class.java

	override fun getViewModelResId(): Int = BR.splashViewModel

	override fun getProgressIndicator(): ProgressBar? {
		return binding.splashProgress
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		hideStatusBars()
		showProgress()
		setUpFirebaseRemoteConfig()
	}

	override fun onResume() {
		super.onResume()

		//Fetching the values here
		mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
			if (task.isSuccessful) {
				mFirebaseRemoteConfig.activate()
				(application as HealthcareApp).setEndpoint(mFirebaseRemoteConfig.getString(END_POINT))
				LocationUpdateManager.getInstance(this).setLocationRequestInterval(
					mFirebaseRemoteConfig.getString(KEY_LOCATION_INTERVAL_IN_MINS).toLong()
				)
				sharedPrefsService.writeStringToSharedPrefs(
					APP_PLAY_STORE_URL_KEY, mFirebaseRemoteConfig.getString(APP_PLAY_STORE_URL_KEY)
				)
				sharedPrefsService.writeStringToSharedPrefs(
					STATISTICS_URL_KEY, mFirebaseRemoteConfig.getString(STATISTICS_URL_KEY)
				)
				sharedPrefsService.writeStringToSharedPrefs(
					IS_STATISTICS_BTN_VISIBLE_KEY, mFirebaseRemoteConfig.getString(IS_STATISTICS_BTN_VISIBLE_KEY)
				)
				sharedPrefsService.writeStringToSharedPrefs(
					KEY_BLUETOOTH_BEACONS_SEND_PERIOD, mFirebaseRemoteConfig.getString(
						KEY_BLUETOOTH_BEACONS_SEND_PERIOD
					)
				)
				//calling function to check if new version is available or not
				checkForUpdate()
			} else {
				hideProgress()
				openMainActivity()
			}
		}
	}

	private fun hideStatusBars() {
		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		)
	}

	private fun setUpFirebaseRemoteConfig() {
		//This is default Map
		firebaseDefaultMap = HashMap()
		//Setting the Default Map Value with the current version code
		firebaseDefaultMap[VERSION_CODE_KEY] = getCurrentVersionCode()
		//Setting that default Map to Firebase Remote Config
		mFirebaseRemoteConfig.setDefaultsAsync(firebaseDefaultMap)
		mFirebaseRemoteConfig.setConfigSettingsAsync(
			FirebaseRemoteConfigSettings.Builder()
				.setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
				.build()
		)
	}

	private fun checkForUpdate() {
		val latestAppVersion = mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY).toInt()
		val isMandatory = mFirebaseRemoteConfig.getBoolean(IS_MANDATORY_KEY)
		if (latestAppVersion > getCurrentVersionCode()) {
			if (!isForceUpdateDialogShown) {
				val alertDialog = AlertDialog.Builder(this)
				alertDialog.setTitle(viewModel.localizeString(NEW_VERSION_LABEL))
					.setMessage(viewModel.localizeString(NEW_VERSION_MSG))
					.setPositiveButton(
						viewModel.localizeString(UPDATE_LABEL)
					) { _, _ ->
						startActivity(
							Intent(
								Intent.ACTION_VIEW,
								Uri.parse(getAppGooglePlayStoreUrl(sharedPrefsService))
							)
						)
					}

				if (!isMandatory) {
					alertDialog.setNegativeButton(viewModel.localizeString(CONTINUE_LABEL)) { dialog, _ ->
						dialog.dismiss()
						openMainActivity()
					}
				}
				alertDialog.setCancelable(false)
				alertDialog.setOnDismissListener {
					isForceUpdateDialogShown = false
				}
				alertDialog.show()
				isForceUpdateDialogShown = true
			}
		} else {
			openMainActivity()
		}
		hideProgress()
	}

	private fun openMainActivity() {
		Handler().postDelayed({
			val url = intent.getStringExtra(URL)
			val intent = Intent(this, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(ACTIVITY_BUNDLE_EXTRA_KEY, Bundle().apply {
				this.putString(URL, url ?: "")
			})
			startActivity(intent)
			finish()
		}, DELAY)
	}

	private fun getCurrentVersionCode(): Long {
		try {
			return PackageInfoCompat.getLongVersionCode(
				packageManager.getPackageInfo(
					packageName,
					0
				)
			)
		} catch (e: PackageManager.NameNotFoundException) {
			e.printStackTrace()
		}
		return -1
	}

	companion object {
		const val STATISTICS_URL_KEY = "statistics_url"
		const val IS_STATISTICS_BTN_VISIBLE_KEY = "is_statistics_btn_visible"
		const val KEY_BLUETOOTH_BEACONS_SEND_PERIOD = "bluetooth_beacons_send_period"
		private const val DEFAULT_APP_GOOGLE_PLAY_STORE_URL =
			"https://play.google.com/store/apps/details?id=bg.government.virusafe"
		private const val DELAY = 1000L
		private const val MINIMUM_FETCH_INTERVAL_SECONDS: Long = 1
		private const val VERSION_CODE_KEY = "latest_app_version"
		private const val APP_PLAY_STORE_URL_KEY = "app_google_play_url"
		private const val END_POINT = "end_point"
		private const val IS_MANDATORY_KEY = "is_mandatory"

		fun getAppGooglePlayStoreUrl(sharedPrefsService: ISharedPrefsService): String {
			val remoteAppGooglePlayUrl = sharedPrefsService.readStringFromSharedPrefs(APP_PLAY_STORE_URL_KEY)

			return if (remoteAppGooglePlayUrl.isBlank()) {
				DEFAULT_APP_GOOGLE_PLAY_STORE_URL
			} else {
				remoteAppGooglePlayUrl
			}
		}
	}
}
