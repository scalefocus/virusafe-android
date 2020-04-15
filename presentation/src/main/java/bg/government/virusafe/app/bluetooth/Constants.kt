package bg.government.virusafe.app.bluetooth

const val AppleManufacturerId = 0x004C
const val ProteGoManufacturerId = 0x08AF
const val ProteGoManufacturerDataVersion = 0x00
// 16-bit Universally Unique Identifier (UUID) for use with ProteGO assigned to Polidea by Bluetooth SIG
// More info: https://www.bluetooth.com/specifications/assigned-numbers/16-bit-uuids-for-members/
const val ProteGoServiceUUIDString = "00002415-0000-1000-8000-00805F9B34FB"
const val ProteGoCharacteristicUUIDString = "b5265acc-4fae-4854-a5aa-e22561fcd423"

const val PeripheralIgnoredTimeoutInSec = 5L
const val PeripheralSynchronizationTimeoutInSec = 20L
const val PeripheralIgnoredGracePeriodIfNoProteGoCharacteristicInMin = 15L
