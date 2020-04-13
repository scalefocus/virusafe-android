package bg.government.virusafe.app.utils

import android.text.InputFilter
import bg.government.virusafe.BuildConfig
import bg.government.virusafe.app.utils.validators.EgnValidator
import bg.government.virusafe.app.utils.validators.EmbgValidator
import java.util.regex.Matcher
import java.util.regex.Pattern

fun getPhoneNumberInputFilter() = arrayOf(
	phoneFilter(),
	InputFilter.LengthFilter(MAX_PHONE_LENGTH)
)

fun getPersonalNumberInputFilter() = arrayOf(
	InputFilter.LengthFilter(getPersonalIdLength())
)

fun getForeignerNumberInputFilter() = arrayOf(
	InputFilter.LengthFilter(FOREIGNER_NUMBER_LENGTH)
)

fun getPassportInputFilter() = arrayOf(
	passportFilter(),
	InputFilter.LengthFilter(MAX_PASSPORT_LENGTH)
)

fun getAgeInputFilter() = arrayOf(
	ageFilter(),
	InputFilter.LengthFilter(MAX_AGE_LENGTH)
)

fun getChronicConditionsInputFilter() = arrayOf(
	chronicConditionsFilter(),
	InputFilter.LengthFilter(MAX_CHRONIC_CONDITIONS_LENGTH)
)

private fun phoneFilter(): InputFilter =
	InputFilter { source, _, _, _, _, _ ->
		val mPattern = Pattern.compile("[0-9+]+")
		val matcher: Matcher = mPattern.matcher(source)
		if (!matcher.matches()) {
			val regionStart = matcher.regionStart()
			val regionEnd = matcher.regionEnd()
			if (regionStart == regionEnd)
				EMPTY_STR
			else
				source.substring(regionStart until regionEnd - 1)
		} else
			null
	}

private fun ageFilter(): InputFilter =
	InputFilter { source, _, _, destination, _, _ ->
		source?.let { src ->
			destination?.let { dest ->
				val ageStr = ("$dest$src").toString()
				val ageNumb = ageStr.toIntOrNull()
				if (ageStr.isBlank())
					return@InputFilter null
				if ((ageNumb in MIN_AGE..MAX_AGE).not()) {
					return@InputFilter EMPTY_STR
				}
			}
		}
		null
	}

private fun chronicConditionsFilter(): InputFilter =
	InputFilter { source, _, _, _, _, _ ->
		val sourceStr = source.toString()
		if (sourceStr == EMPTY_STR) {
			return@InputFilter source
		}

		if (source.isNotEmpty() &&
			sourceStr.matches("[a-zA-Z0-9 ,.()а-яА-Я-]+".toRegex())
		) {
			return@InputFilter source
		} else {
			return@InputFilter sourceStr.replace(
				"[^a-zA-Z0-9 ,.()а-яА-Я-]+".toRegex(),
				EMPTY_STR
			)
		}
	}

private fun passportFilter(): InputFilter =
	InputFilter { source, _, _, _, _, _ ->
		val sourceStr = source.toString()
		if (sourceStr == EMPTY_STR) {
			return@InputFilter source
		}

		if (source.isNotEmpty() && sourceStr.matches("[a-zA-Z0-9]+".toRegex())) {
			return@InputFilter source
		} else {
			return@InputFilter sourceStr.replace("[^a-zA-Z0-9]".toRegex(), EMPTY_STR)
		}
	}

fun getMinPhoneLength() = when (BuildConfig.BUILD_TYPE) {
	BUILD_TYPE_MK -> MIN_PHONE_LENGTH_MK
	else -> MIN_PHONE_LENGTH_BG
}

fun getPersonalIdValidator() = when (BuildConfig.BUILD_TYPE) {
	BUILD_TYPE_MK -> EmbgValidator()
	else -> EgnValidator()
}

fun hasValidPersonalIdLength(personalIdStr: String) =
	personalIdStr.length == getPersonalIdLength()

fun hasValidForeignerNumberLength(numberStr: String) =
	numberStr.length == FOREIGNER_NUMBER_LENGTH

fun hasValidPassportLength(passportStr: String) =
	passportStr.length in MIN_PASSPORT_LENGTH..MAX_PASSPORT_LENGTH

private fun getPersonalIdLength() = when (BuildConfig.BUILD_TYPE) {
	BUILD_TYPE_MK -> PERSONAL_ID_LENGTH_MK
	else -> PERSONAL_ID_LENGTH_BG
}

//base constants
private const val EMPTY_STR = ""
private const val MIN_AGE = 1
private const val MAX_AGE = 150
private const val MAX_AGE_LENGTH = 3
private const val MAX_CHRONIC_CONDITIONS_LENGTH = 100
private const val MAX_PHONE_LENGTH = 15
private const val MAX_PASSPORT_LENGTH = 20
private const val MIN_PASSPORT_LENGTH = 5
private const val FOREIGNER_NUMBER_LENGTH = 10

//depends on build type
private const val MIN_PHONE_LENGTH_BG = 10
private const val MIN_PHONE_LENGTH_MK = 9
private const val PERSONAL_ID_LENGTH_BG = 10
private const val PERSONAL_ID_LENGTH_MK = 13
