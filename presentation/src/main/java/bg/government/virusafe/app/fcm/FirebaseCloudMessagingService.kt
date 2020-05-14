package bg.government.virusafe.app.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import bg.government.virusafe.R
import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.splash.SplashActivity
import bg.government.virusafe.mvvm.application.HealthcareApp
import bg.government.virusafe.mvvm.di.IAppComponent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.upnetix.applicationservice.pushtoken.IPushTokenService
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.HAS_REGISTRATION_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.USE_PERSONAL_DATA_KEY
import com.upnetix.presentation.BaseApplication
import com.upnetix.presentation.navigation.ACTIVITY_BUNDLE_EXTRA_KEY
import com.upnetix.service.sharedprefs.ISharedPrefsService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class FirebaseCloudMessagingService : FirebaseMessagingService() {

	@Inject
	lateinit var tokenService: IPushTokenService

	@Inject
	lateinit var sharedPrefs: ISharedPrefsService

	override fun onCreate() {
		super.onCreate()
		val component = getAppComponent()
		injectSelf(component)
	}

	private fun injectSelf(appComponent: IAppComponent) {
		appComponent.inject(this)
	}

	private fun getAppComponent(): IAppComponent {

		val baseApp = application as BaseApplication<*>
		return baseApp.appComponent as IAppComponent
	}

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		// Disable notifications if the user has denied consent
		if (!sharedPrefs.readStringFromSharedPrefs(USE_PERSONAL_DATA_KEY).toBoolean()) return

		showNotification(remoteMessage)
	}

	@SuppressLint("WrongConstant")
	private fun showNotification(remoteMessage: RemoteMessage) {
		val notification = remoteMessage.notification
		val activity =
			if ((this.applicationContext as HealthcareApp).getCurrentActivity() is MainActivity) {
				MainActivity::class.java
			} else {
				SplashActivity::class.java
			}

		val intent = Intent(this, activity)
		if (!remoteMessage.data[URL].isNullOrEmpty()) {
			intent.putExtra(ACTIVITY_BUNDLE_EXTRA_KEY, Bundle().apply {
				putString(URL, remoteMessage.data[URL])
			})
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		val pendingIntent: PendingIntent = PendingIntent.getActivity(
			this, 0, intent,
			PendingIntent.FLAG_ONE_SHOT
		)
		val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
		val notificationBuilder: NotificationCompat.Builder =
			NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_app_logo)
				.setColor(ContextCompat.getColor(this, R.color.colorPrimary))
				.setContentTitle(notification?.title)
				.setContentText(notification?.body)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.setVibrate(longArrayOf(VIBRATION, VIBRATION))
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent)
		val notificationManager: NotificationManager =
			getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				getString(R.string.app_name),
				NotificationManager.IMPORTANCE_HIGH
			)
			channel.description = getString(R.string.app_name)
			notificationManager.createNotificationChannel(channel)
			notificationBuilder.setChannelId(channel.id)
		}
		notificationManager.notify(
			Calendar.getInstance().timeInMillis.toInt(),
			notificationBuilder.build()
		)
	}

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		if (sharedPrefs.readStringFromSharedPrefs(HAS_REGISTRATION_KEY).isNotBlank()) {
			GlobalScope.launch {
				tokenService.sendPushToken(token)
			}
		} else {
			sharedPrefs.writeStringToSharedPrefs(FIREBASE_TOKEN, token)
		}
	}

	companion object {
		const val URL = "url"
		const val KEY_LOCATION_INTERVAL_IN_MINS = "location_interval_in_mins"

		private const val NOTIFICATION_CHANNEL_ID = "viru_safe_channel_id"
		private const val VIBRATION = 1000L
		const val FIREBASE_TOKEN = "om.scalefocus.virusafe.key4"
	}
}
