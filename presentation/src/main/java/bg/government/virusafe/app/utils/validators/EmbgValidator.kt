@file:Suppress("MagicNumber")

package bg.government.virusafe.app.utils.validators

import com.upnetix.applicationservice.registration.model.Gender
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

/**
 * Validate EMBG.
 */
private const val EMBG_SKIP_FACTOR = 6
private const val EMBG_MOD = 11
private const val EMBG_BASE = 11

class EmbgValidator : PersonalIdValidator {

	private val embgWeights = listOf(7, 6, 5, 4, 3, 2)
	private var personalNumber: String? = null
	private var isValid = false

	override var years: Long? = null
		private set

	override var gender: String? = null
		private set

	override fun isValidPersonalId(): Boolean = isValid

	override fun initPersonalNumber(personalNumber: String?) {
		this.personalNumber = personalNumber
		isValid = isValidPersonalNumber()
		if (!isValid) {
			years = null
			gender = null
		}
	}

	override fun clearAll() {
		personalNumber = null
		isValid = false
		years = null
		gender = null
	}

	@Suppress("ReturnCount")
	private fun isValidPersonalNumber(): Boolean {
		if (personalNumber.isNullOrEmpty()) {
			return true
		} else if (personalNumber?.length != 13) {
			return false
		}

		val charArr = personalNumber!!.toCharArray()
		val personalNumberDigits: MutableList<Int> = ArrayList(charArr.size)

		for (c in charArr) {
			personalNumberDigits.add(c - '0')
		}

		updateGender(personalNumberDigits)
		val isValidBirthday: Boolean = validateBirthday(personalNumberDigits)
		val isValidEmbgChecksum: Boolean = validateChecksum(personalNumberDigits)

		return isValidBirthday && isValidEmbgChecksum
	}

	private fun updateGender(personalNumberDigits: List<Int>) {
		val genderUniqueNumber =
			personalNumberDigits[9] * 100 + personalNumberDigits[10] * 10 + personalNumberDigits[11]
		gender = if (genderUniqueNumber > 499) {
			Gender.VALUE_FEMALE
		} else {
			Gender.VALUE_MALE
		}
	}

	@Suppress("TooGenericExceptionCaught")
	private fun validateBirthday(personalNumberDigits: List<Int>): Boolean {
		var year = 1000 + personalNumberDigits[4] * 100 + personalNumberDigits[5] * 10 + personalNumberDigits[6]
		val month = personalNumberDigits[2] * 10 + personalNumberDigits[3]
		val day = personalNumberDigits[0] * 10 + personalNumberDigits[1]

		// Handle years after 2000.
		if (year <= 1800) {
			year += 1000
		}

		years = try {
			val birthday = LocalDate.of(year, month, day)
			val now = LocalDate.now()
			ChronoUnit.YEARS.between(birthday, now)
		} catch (e: Exception) {
			return false
		}

		return true
	}

	private fun validateChecksum(personalNumberDigits: List<Int>): Boolean {
		var checkSum = 0
		for (i in embgWeights.indices) {
			checkSum += ((personalNumberDigits[i] + personalNumberDigits[i + EMBG_SKIP_FACTOR])
					* embgWeights[i])
		}

		checkSum %= EMBG_MOD
		checkSum = EMBG_BASE - checkSum

		if (checkSum > 9) {
			checkSum = 0
		}

		return personalNumberDigits[12] == checkSum
	}
}
