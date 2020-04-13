package bg.government.virusafe.app.utils.validators

class LncValidator {

	private val LNCH_WEIGHTS: List<Int> =
		listOf(21, 19, 17, 13, 11, 9, 7, 3, 1)
	private val LNCH_MOD = 10

	fun isValid(foreignerNumber: String): Boolean {
		val charArr = foreignerNumber.toCharArray()
		val lnchDigits: MutableList<Int> =
			ArrayList(charArr.size)

		for (c in charArr) {
			lnchDigits.add(c - '0')
		}

		var checkSum = 0
		for (i in 0 until lnchDigits.size - 1) {
			checkSum += lnchDigits[i] * LNCH_WEIGHTS[i]
		}

		checkSum %= LNCH_MOD
		return lnchDigits[9] == checkSum
	}
}