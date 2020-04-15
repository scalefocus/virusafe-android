package bg.government.virusafe.app.bluetooth

fun ByteArray.toHexString(): String {
	return this.joinToString("") {
		String.format("%02x", it)
	}
}