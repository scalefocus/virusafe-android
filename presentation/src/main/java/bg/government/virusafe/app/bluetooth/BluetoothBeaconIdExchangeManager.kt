package bg.government.virusafe.app.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import bg.government.virusafe.app.bluetooth.advertiser.AdvertiserInterface
import bg.government.virusafe.app.bluetooth.advertiser.AdvertiserListener
import bg.government.virusafe.app.bluetooth.advertiser.AdvertiserResult
import bg.government.virusafe.app.bluetooth.advertiser.EnableResult
import bg.government.virusafe.app.bluetooth.advertiser.ProteGoAdvertiser
import bg.government.virusafe.app.bluetooth.advertiser.ServerResult
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdAgent
import bg.government.virusafe.app.bluetooth.scanner.ProteGoScanner
import bg.government.virusafe.app.bluetooth.scanner.ScannerInterface
import bg.government.virusafe.app.bluetooth.scanner.ScannerListener
import timber.log.Timber

class BluetoothBeaconIdExchangeManager(private val context: Context, private val beaconIdAgent: BeaconIdAgent) {

	private fun timberWithLocalTag() = Timber.tag("[BT_MNG]")

	private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
	private val proteGoAdvertiser =
		ProteGoAdvertiser(context, bluetoothManager, beaconIdAgent, object : AdvertiserListener {
			override fun error(
				advertiserInterface: AdvertiserInterface,
				advertiserError: AdvertiserListener.AdvertiserError
			) {
				timberWithLocalTag().e("advertiser error: $advertiserError")
			}
		})
	private var scannerInitialized = false
	private val proteGoScanner: ProteGoScanner by lazy {
		scannerInitialized = true
		ProteGoScanner(context, object : ScannerListener {
			override fun error(scannerInterface: ScannerInterface, throwable: Throwable) {
				timberWithLocalTag().e(throwable, "scanner error")
			}
		}, beaconIdAgent)
	}

	fun start(mode: Mode = Mode.BEST_EFFORT) {
		when (mode) {
			Mode.CONNECT_ONLY -> proteGoScanner.enable(ScannerInterface.Mode.SCAN_AND_EXCHANGE_BEACON_IDS)
			Mode.BEST_EFFORT -> when (val enableResult = proteGoAdvertiser.enable()) {
				EnableResult.PreconditionFailure.AlreadyEnabled,
				EnableResult.PreconditionFailure.CannotObtainBluetoothAdapter,
				EnableResult.PreconditionFailure.BluetoothOff -> timberWithLocalTag().e("fatal error: $enableResult")

				is EnableResult.Started -> {
					timberWithLocalTag().i("advertiser enabled: $enableResult")
					val scannerMode = if (
						enableResult.advertiserResult == AdvertiserResult.Success
						&& enableResult.serverResult is ServerResult.Success
					) {
						ScannerInterface.Mode.SCAN_ONLY
					} else {
						ScannerInterface.Mode.SCAN_AND_EXCHANGE_BEACON_IDS
					}
					proteGoScanner.enable(scannerMode)
					if (enableResult.advertiserResult !is AdvertiserResult.Success) {
						// no use for advertiser nor server
						proteGoAdvertiser.disable()
					}
				}
			}
		}
	}

	fun stop() {
		proteGoAdvertiser.disable()
		if (scannerInitialized) proteGoScanner.disable()
	}

	enum class Mode {
		BEST_EFFORT,
		CONNECT_ONLY
	}
}