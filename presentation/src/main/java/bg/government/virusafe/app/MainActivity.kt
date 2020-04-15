package bg.government.virusafe.app

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.widget.ProgressBar
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager.getInstance
import bg.government.virusafe.R
import bg.government.virusafe.app.bluetooth.BluetoothProximityService
import bg.government.virusafe.app.bluetooth.proximity.ProximityData
import bg.government.virusafe.app.bluetooth.proximity.ProximityWorker
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService.Companion.URL
import bg.government.virusafe.app.home.HomeFragment
import bg.government.virusafe.app.home.HomeFragment.Companion.CHECK_BLUETOOTH
import bg.government.virusafe.app.localization.LocalizationFragment
import bg.government.virusafe.app.location.LocationUpdateManager
import bg.government.virusafe.app.personaldata.PersonalDataFragment
import bg.government.virusafe.app.registration.CodeVerificationFragment
import bg.government.virusafe.app.registration.CodeVerificationFragment.Companion.SMS_RETRIEVE_STATUS_CODE
import bg.government.virusafe.app.registration.RegistrationFragment
import bg.government.virusafe.app.registration.SmsBroadcastReceiver
import bg.government.virusafe.app.selfcheck.DoneFragment
import bg.government.virusafe.app.splash.SplashActivity.Companion.KEY_BLUETOOTH_BEACONS_SEND_PERIOD
import bg.government.virusafe.app.utils.LOCATION_PERMISSION_MSG
import bg.government.virusafe.app.utils.NO_LABEL
import bg.government.virusafe.app.utils.OK_LABEL
import bg.government.virusafe.app.utils.WARNING_LABEL
import bg.government.virusafe.app.utils.YES_LABEL
import bg.government.virusafe.databinding.ActivityMainBinding
import bg.government.virusafe.mvvm.activity.AbstractActivity
import bg.government.virusafe.mvvm.application.HealthcareApp
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.FINISHED_REGISTRATION_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.HAS_REGISTRATION_KEY
import com.upnetix.presentation.view.DEFAULT_VIEW_MODEL_ID
import com.upnetix.presentation.view.IView
import com.upnetix.service.sharedprefs.ISharedPrefsService
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AbstractActivity<ActivityMainBinding, EmptyViewModel>(),
	EasyPermissions.PermissionCallbacks {

	@Inject
	lateinit var sharedPrefsService: ISharedPrefsService

	private var isReturningFromSettings = false

	private var isWaitingForSms = false

	private var locationPermissionListener: (() -> Unit)? = null

	private var smsBroadcastReceiver: SmsBroadcastReceiver? = null

	override fun getLayoutResId() = R.layout.activity_main

	override fun getViewModelClass() = EmptyViewModel::class.java

	override fun getViewModelResId() = DEFAULT_VIEW_MODEL_ID

	override fun getContainerViewId() = R.id.main_container

	override fun getProgressIndicator(): ProgressBar? {
		return binding.mainProgress
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		decideNavigation()
		checkForNotificationData()
		if (!isMyServiceRunning(BluetoothProximityService::class.java)) {
			(this.applicationContext as HealthcareApp).startBluetoothLeExchange()
		}
		enqueueSendProximityRequest()
	}

	private fun decideNavigation() {
		val hasRegistration = sharedPrefsService.readStringFromSharedPrefs(HAS_REGISTRATION_KEY).isNotEmpty()
		if (hasRegistration) {
			val hasFinishedRegistrationFlow =
				sharedPreferences.readStringFromSharedPrefs(FINISHED_REGISTRATION_KEY).isNotEmpty()
			if (hasFinishedRegistrationFlow) {
				openView(HomeFragment::class)
			} else {
				openFragmentFromRegistrationFlow(PersonalDataFragment::class)
			}
		} else {
			openFragmentFromRegistrationFlow(LocalizationFragment::class)
		}
	}

	private fun checkForNotificationData() {
		val url = navigationArgs?.getString(URL)
		if (!url.isNullOrEmpty()) {
			if (Patterns.WEB_URL.matcher(url).matches()) {
				openView(WebViewFragment::class, Bundle().apply {
					this.putString(URL, url)
				})
			}
		}
	}

	override fun onViewChanged(oldView: IView<*>?, newView: IView<*>) {
		when (newView) {
			is RegistrationFragment -> startSmsRetrieve()

			is HomeFragment, is DoneFragment -> {
				isWaitingForSms = false
				stopSmsRetrieve()
			}
		}
	}

	private fun enqueueSendProximityRequest() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val work = PeriodicWorkRequest.Builder(
			ProximityWorker::class.java,
			sendProximityRequestPeriodInMins(),
			TimeUnit.MINUTES
		).setConstraints(constraints).build()

		getInstance(this).enqueueUniquePeriodicWork(SEND_PROXIMITIES, ExistingPeriodicWorkPolicy.KEEP, work)
	}

	private fun sendProximityRequestPeriodInMins(): Long {
		val sendPeriodStr =
			sharedPreferences.readStringFromSharedPrefs(KEY_BLUETOOTH_BEACONS_SEND_PERIOD)
		var sendPeriod = 15L
		if (sendPeriodStr.isNotBlank()) {
			try {
				sendPeriod = sendPeriodStr.toLong()
			} catch (e: NumberFormatException) {
				e.printStackTrace()
			}
		}

		return sendPeriod
	}

	override fun onResume() {
		super.onResume()
		if (isWaitingForSms) {
			startSmsBroadcastReceiver()
		}
		if (isReturningFromSettings) {
			isReturningFromSettings = false
			startLocationTracking()
		}
	}

	override fun onPause() {
		super.onPause()
		if (isWaitingForSms) {
			stopSmsRetrieve()
		}
	}

	override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
		if (getPermissions().size == perms.size) {
			locationPermissionListener?.invoke()
		}
	}

	override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
		onPermissionGranted()
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				LOCATION_PERMISSIONS_REQUEST_CODE -> {
					startLocationTracking()
					(this.applicationContext as HealthcareApp).startBluetoothLeExchange()
				}

				REQUEST_ENABLE_BT -> {
					requestLocationTracking {}
				}
			}
		} else {
			locationPermissionListener?.invoke()
		}

		if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
			if (hasLocationPermissions()) {
				startLocationTracking()
				(this.applicationContext as HealthcareApp).startBluetoothLeExchange()
			} else {
				locationPermissionListener?.invoke()
			}
		}
	}

	@Inject
	lateinit var sharedPreferences: ISharedPrefsService

	fun requestLocationTracking(listener: () -> Unit) {
		locationPermissionListener = listener

		if (!hasLocationPermissions()) {
			val somePermissionDenied = somePermissionDenied()
			if (somePermissionDenied) {
				requestLocationPermissions()
			} else {
				if (sharedPreferences.readStringFromSharedPrefs(KEY_SHOW).isEmpty()) {
					showPermissionDescDialog()
				} else {
					if (!somePermissionDenied && somePermissionPermanentlyDenied()) {
						showAppSettingsDialog()
					} else {
						requestLocationPermissions()
					}
				}
			}
		} else {
			onPermissionGranted()
		}
	}

	fun verifyBluetooth() {
		try {
			if (!(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled) {
				val intentBtEnabled = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
				startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT)
			} else {
				requestLocationTracking {}
			}
		} catch (e: RuntimeException) {
			// Device does not support Bluetooth LE
			e.printStackTrace()
		}
	}

	private fun showPermissionDescDialog() {
		AlertDialog.Builder(this).setTitle(viewModel.localizeString(WARNING_LABEL))
			.setMessage(viewModel.localizeString(LOCATION_PERMISSION_MSG))
			.setPositiveButton(viewModel.localizeString(OK_LABEL), null)
			.setOnDismissListener {
				requestLocationPermissions()
				sharedPreferences.writeStringToSharedPrefs(KEY_SHOW, VALUE_SHOW)
			}
			.show()
	}

	private fun showAppSettingsDialog() {
		AppSettingsDialog.Builder(this).setTitle(viewModel.localizeString(WARNING_LABEL))
			.setRationale(viewModel.localizeString(LOCATION_PERMISSION_MSG))
			.setPositiveButton(viewModel.localizeString(YES_LABEL))
			.setNegativeButton(viewModel.localizeString(NO_LABEL)).build()
			.show()
	}

	private fun onPermissionGranted() {
		if (!isGpsEnabled()) {
			requestGpsProvider()
			return
		}

		(this.applicationContext as HealthcareApp).startBluetoothLeExchange()
		startLocationTracking()
	}

	private fun startLocationTracking() {
		LocationUpdateManager.getInstance(this).startLocationUpdates()
		locationPermissionListener?.invoke()
	}

	private fun buildLocationSettingsRequest() = LocationSettingsRequest.Builder()
		.addLocationRequest(LocationUpdateManager.getInstance(this).locationRequest)
		.setAlwaysShow(true)
		.build()

	private fun requestGpsProvider() {

		val locationSettingsRequest = buildLocationSettingsRequest()
		val settingsResult =
			LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest)

		settingsResult.addOnCompleteListener {
			try {
				val settingsResponse = it.getResult(ApiException::class.java)
				if (settingsResponse != null) {
					startLocationTracking()
					(this.applicationContext as HealthcareApp).startBluetoothLeExchange()
				}
			} catch (ex: ApiException) {
				when (ex.statusCode) {
					LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> onResolutionRequired(ex)
					LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> onSettingsChangeUnavailable()
				}
			}
		}
	}

	private fun onSettingsChangeUnavailable() {
		isReturningFromSettings = true
		openLocationSettings()
	}

	private fun onResolutionRequired(ex: ApiException) {
		val resolvableException = ex as? ResolvableApiException
		val intentSender = resolvableException?.resolution?.intentSender
		try {
			startIntentSenderForResult(
				intentSender, LOCATION_PERMISSIONS_REQUEST_CODE,
				null, 0, 0, 0, null
			)
		} catch (sendIntentEx: IntentSender.SendIntentException) {
			//do nothing by docs this should never happen
		}
	}

	private fun openLocationSettings() {
		val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
		startActivity(intent)
	}

	@Suppress("SpreadOperator")
	private fun requestLocationPermissions() {
		EasyPermissions.requestPermissions(
			this,
			viewModel.localizeString(LOCATION_PERMISSION_MSG),
			LOCATION_PERMISSIONS_REQUEST_CODE,
			*getPermissions()
		)
	}

	private fun getPermissions(): Array<String> {
		val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
		}
		return permissions.toTypedArray()
	}

	private fun hasLocationPermissions(): Boolean {
		val fineLocationAccess =
			EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
		var backgroundLocationAccess = true
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			backgroundLocationAccess =
				EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
		}
		return fineLocationAccess && backgroundLocationAccess
	}

	private fun somePermissionPermanentlyDenied(): Boolean {

		val fineLocationDenied = EasyPermissions.permissionPermanentlyDenied(
			this,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
		var backgroundLocationDenied = false
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			backgroundLocationDenied = EasyPermissions.permissionPermanentlyDenied(
				this,
				Manifest.permission.ACCESS_BACKGROUND_LOCATION
			)
		}
		return fineLocationDenied || backgroundLocationDenied
	}

	private fun somePermissionDenied(): Boolean {

		val fineLocationDenied = EasyPermissions.somePermissionDenied(
			this,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
		var backgroundLocationDenied = false
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			backgroundLocationDenied = EasyPermissions.somePermissionDenied(
				this,
				Manifest.permission.ACCESS_BACKGROUND_LOCATION
			)
		}
		return fineLocationDenied || backgroundLocationDenied
	}

	private fun getSystemLocationService(): LocationManager =
		getSystemService(Context.LOCATION_SERVICE) as LocationManager

	private fun isGpsEnabled(): Boolean =
		getSystemLocationService().isProviderEnabled(LocationManager.GPS_PROVIDER)

	private fun startSmsRetrieve() {
		SmsRetriever.getClient(this).also {
			it.startSmsUserConsent(null)
				.addOnSuccessListener {
					isWaitingForSms = true
					startSmsBroadcastReceiver()
				}
		}
	}

	private fun startSmsBroadcastReceiver() {
		if (smsBroadcastReceiver == null) {
			smsBroadcastReceiver = SmsBroadcastReceiver().also {
				it.setListener(
					object : SmsBroadcastReceiver.SmsBroadcastReceiverListener() {
						override fun onSuccess(intent: Intent?) {
							intent?.let { context ->
								(currentView as? CodeVerificationFragment)?.startActivityForResult(
									context,
									SMS_RETRIEVE_STATUS_CODE
								)
							}
						}
					}
				)
			}
		}

		val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
		registerReceiver(smsBroadcastReceiver, intentFilter)
	}

	private fun stopSmsRetrieve() {
		smsBroadcastReceiver?.let {
			unregisterReceiver(it)
		}
	}

	@Suppress("DEPRECATION")
	private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
		val manager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		for (service in manager.getRunningServices(Int.MAX_VALUE)) {
			if (serviceClass.name == service.service.className) {
				return true
			}
		}
		return false
	}

	override fun onDestroy() {
		ProximityData.saveProximities(this, mutableListOf())
		sharedPreferences.clearValue(CHECK_BLUETOOTH)
		sharedPreferences.clearValue("beacons")

		super.onDestroy()
	}

	companion object {
		private const val LOCATION_PERMISSIONS_REQUEST_CODE = 999
		private const val REQUEST_ENABLE_BT: Int = 1
		private const val SEND_PROXIMITIES = "send_proximities"
		private const val KEY_SHOW = "key_show"
		private const val VALUE_SHOW = "value_show"
	}
}
