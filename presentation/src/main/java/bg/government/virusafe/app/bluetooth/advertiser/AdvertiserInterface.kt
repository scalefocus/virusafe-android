package bg.government.virusafe.app.bluetooth.advertiser

interface AdvertiserInterface {

	/**
	 * Function that enables the [AdvertiserInterface]
	 * @return the result of enabling
	 */
	fun enable(): EnableResult

	/**
	 * Method that disables the [AdvertiserInterface]
	 */
	fun disable()
}