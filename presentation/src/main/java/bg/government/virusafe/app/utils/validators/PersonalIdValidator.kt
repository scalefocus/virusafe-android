package bg.government.virusafe.app.utils.validators

/**
 * Use to validate the personal id number.
 */
interface PersonalIdValidator {

	/**
	 * Initialize the validator with the given string.
	 *
	 * @param personalNumber the personal number as a string
	 */
	fun initPersonalNumber(personalNumber: String?)

	/**
	 * @return the gender based on the personal number
	 */
	val gender: String?

	/**
	 * @return the years based on the personal number
	 */
	val years: Long?

	/**
	 * Check if this is valid personal id.
	 */
	fun isValidPersonalId(): Boolean

	/**
	 * Set all of the properties to null.
	 */
	fun clearAll()
}
