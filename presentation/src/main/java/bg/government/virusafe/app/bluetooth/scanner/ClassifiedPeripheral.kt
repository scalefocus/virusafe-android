package bg.government.virusafe.app.bluetooth.scanner

import bg.government.virusafe.app.bluetooth.beacon.BeaconId
import com.polidea.rxandroidble2.RxBleDevice

sealed class ClassifiedPeripheral {
	sealed class ProteGo(val bleDevice: RxBleDevice) : ClassifiedPeripheral() {
		class FullAdvertisement(bleDevice: RxBleDevice, val beaconId: BeaconId, val rssi: Int) : ProteGo(bleDevice)

		// possibly PartialAdvertisement with partial beaconId
		class MinimalAdvertisement(bleDevice: RxBleDevice) : ProteGo(bleDevice)
		class PotentialAdvertisement(bleDevice: RxBleDevice) : ProteGo(bleDevice)
	}

	object NonProteGo : ClassifiedPeripheral()

	fun className(): String = when (this) {
		is ProteGo.FullAdvertisement -> "ProteGo.FullAdvertisement"
		is ProteGo.MinimalAdvertisement -> "ProteGo.MinimalAdvertisement"
		is ProteGo.PotentialAdvertisement -> "ProteGo.PotentialAdvertisement"
		NonProteGo -> "NonProteGo"
	}
}