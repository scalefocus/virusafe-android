package bg.government.virusafe.app.bluetooth.beacon

data class BeaconIdRemote(
	val beaconId: BeaconId,
	val rssi: Int?,
	val source: BeaconIdSource,
	val bluetoothMacAddress: String
)