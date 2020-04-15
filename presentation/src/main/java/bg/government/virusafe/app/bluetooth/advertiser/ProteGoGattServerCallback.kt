package bg.government.virusafe.app.bluetooth.advertiser

interface ProteGoGattServerCallback {
	fun gattServerStarted(gattServer: ProteGoGattServer)
	fun gattServerFailed(gattServer: ProteGoGattServer, status: Int)
}