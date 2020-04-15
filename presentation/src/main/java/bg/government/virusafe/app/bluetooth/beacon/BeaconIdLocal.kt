package bg.government.virusafe.app.bluetooth.beacon

import java.util.*

data class BeaconIdLocal(val beaconId: BeaconId, val expirationDate: Date, val bluetoothMacAddress: String)