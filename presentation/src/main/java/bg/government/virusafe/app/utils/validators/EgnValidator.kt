@file:Suppress("MagicNumber")
package bg.government.virusafe.app.utils.validators

import bg.government.virusafe.app.utils.empty
import com.upnetix.applicationservice.registration.model.Gender
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * Validate EGN.
 */
private const val EGN_MOD = 11

class EgnValidator : PersonalIdValidator {

	private val egnWeights = listOf(2, 4, 8, 5, 10, 9, 7, 3, 6)
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
		if (personalNumber == null || personalNumber == String.empty) {
			return true
		} else if (personalNumber!!.length != 10) {
			return false
		}

		val charArr = personalNumber!!.toCharArray()
		val personalNumberDigits: MutableList<Int> = ArrayList(charArr.size)

		for (c in charArr) {
			personalNumberDigits.add(c - '0')
		}

		updateGender(personalNumberDigits)

		val isValidBirthday: Boolean = validateBirthday(personalNumberDigits)
		val isValidEgnChecksum: Boolean = validateEgnCheckSum(personalNumberDigits)

		return isValidBirthday && isValidEgnChecksum
	}

	private fun updateGender(egnDigits: List<Int>) {
		val c = egnDigits[8]
		gender = if (c % 2 == 0) {
			Gender.VALUE_MALE
		} else {
			Gender.VALUE_FEMALE
		}
	}

	@Suppress("TooGenericExceptionCaught, LongMethod")
	private fun validateBirthday(egnDigits: List<Int>): Boolean {
		var year = egnDigits[0] * 10 + egnDigits[1]
		var month = egnDigits[2] * 10 + egnDigits[3]
		val day = egnDigits[4] * 10 + egnDigits[5]

		// Handle month offsets based on birth years before 1900 and after 2000.
		when {
			month > 40 -> {
				month -= 40
				year += 2000
			}
			month > 20 -> {
				month -= 20
				year += 1800
			}
			else -> {
				year += 1900
			}
		}

		years = try {
			val birthDate = LocalDate.of(year, month, day)
			val now = LocalDate.now()
			ChronoUnit.YEARS.between(birthDate, now)
		} catch (e: Exception) {
			return false
		}

		return true
	}

	private fun validateEgnCheckSum(egnDigits: List<Int>): Boolean {
		var checkSum = 0
		for (i in 0 until egnDigits.size - 1) {
			checkSum += egnDigits[i] * egnWeights[i]
		}

		checkSum %= EGN_MOD
		if (checkSum == 10) {
			checkSum = 0
		}

		return egnDigits[9] == checkSum
	}
}
