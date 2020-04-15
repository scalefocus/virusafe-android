package bg.government.virusafe.app.bluetooth

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import bg.government.virusafe.R
import com.upnetix.service.sharedprefs.ISharedPrefsService
import timber.log.Timber
import javax.inject.Inject

class BluetoothProximityService : Service() {

	companion object {
		private const val NOTIFICATION_CHANNEL_ID = "bluetooth_le_channel_id"
		private const val START_EXCHANGE_DELAY = 3000L
		private const val FOREGROUND_NOTIFICATION_ID = 500
	}

	private var mWakeLock: PowerManager.WakeLock? = null

	@Inject
	lateinit var sharedPrefs: ISharedPrefsService

	private lateinit var bluetoothBeaconIdExchangeManager: BluetoothBeaconIdExchangeManager

	override fun onCreate() {
		super.onCreate()
		Timber.i("onCreate()")
		bluetoothBeaconIdExchangeManager = BluetoothBeaconIdExchangeManager(
			this, BeaconIdManager(this)
		)

		startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Timber.i("onStartCommand()")

		bluetoothBeaconIdExchangeManager.stop()

		Handler().postDelayed(Runnable {
			bluetoothBeaconIdExchangeManager.start()
		}, START_EXCHANGE_DELAY)

		if(mWakeLock == null) {
			val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
			mWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"BluetoothProximityService::lock"
			)
			mWakeLock?.acquire()
		}

		return START_STICKY
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun onDestroy() {
		Timber.i("onDestroy()")

		mWakeLock?.let {
			if(it.isHeld)
				it.release()
		}

		super.onDestroy()
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		Timber.i("onTaskRemoved()")

		super.onTaskRemoved(rootIntent)
	}

	override fun onTrimMemory(level: Int) {
		Timber.i("onTrimMemory()")

		super.onTrimMemory(level)
	}

	private fun createForegroundNotification(): Notification {
		val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
		builder.setSmallIcon(R.drawable.ic_app_logo)
		builder.setContentTitle(getString(R.string.scan_for_beacons))
		builder.setOngoing(true)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
			)
			channel.description = getString(R.string.app_name)
			val notificationManager = getSystemService(
				Context.NOTIFICATION_SERVICE
			) as NotificationManager
			notificationManager.createNotificationChannel(channel)
			builder.setChannelId(channel.id)
		}

		return builder.build()
	}
}