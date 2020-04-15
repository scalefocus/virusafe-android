package bg.government.virusafe.app.bluetooth.scanner

import android.os.ParcelUuid
import bg.government.virusafe.app.bluetooth.AppleManufacturerId
import bg.government.virusafe.app.bluetooth.ProteGoManufacturerId
import bg.government.virusafe.app.bluetooth.ProteGoServiceUUIDString
import bg.government.virusafe.app.bluetooth.beacon.BeaconId
import com.polidea.rxandroidble2.scan.ScanRecord
import com.polidea.rxandroidble2.scan.ScanResult

object ScanResultClassification : (ScanResult) -> ClassifiedPeripheral {

	private object ProteGoAdvertisement {
		val searchedUuid: ParcelUuid = ParcelUuid.fromString(ProteGoServiceUUIDString)
		operator fun contains(scanRecord: ScanRecord) = scanRecord.serviceUuids?.contains(searchedUuid) ?: false
	}

	private object AppleAdvertisement {
		operator fun contains(scanRecord: ScanRecord) = scanRecord.manufacturerSpecificData[AppleManufacturerId] != null
	}

	override fun invoke(scanResult: ScanResult): ClassifiedPeripheral {
		val bleDevice = scanResult.bleDevice
		return when (scanResult.scanRecord) {
			in ProteGoAdvertisement -> potentialBeaconIdOf(scanResult.scanRecord).let {
				if (it != null) ClassifiedPeripheral.ProteGo.FullAdvertisement(bleDevice, it, scanResult.rssi)
				else ClassifiedPeripheral.ProteGo.MinimalAdvertisement(bleDevice)
			}
			in AppleAdvertisement -> ClassifiedPeripheral.ProteGo.PotentialAdvertisement(bleDevice)
			else -> ClassifiedPeripheral.NonProteGo
		}
	}

	private fun potentialBeaconIdOf(scanRecord: ScanRecord): BeaconId? =
		scanRecord.manufacturerSpecificData[ProteGoManufacturerId]
			?.let {
				val expectedManufacturerSpecificDataSize = BeaconId.byteCount + 1 /* versioning */
				if (it.size == expectedManufacturerSpecificDataSize) {
					BeaconId(it.copyOfRange(1, expectedManufacturerSpecificDataSize), it[0].toInt())
				} else {
					null
				}
			}
}