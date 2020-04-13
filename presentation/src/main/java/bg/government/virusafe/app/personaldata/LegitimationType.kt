package bg.government.virusafe.app.personaldata

import bg.government.virusafe.BuildConfig
import bg.government.virusafe.app.utils.BUILD_TYPE_MK

enum class LegitimationType {
	PERSONAL_NUMBER {
		override fun toString(): String = getPersonalNumberType()
	},
	FOREIGNER_NUMBER {
		override fun toString(): String = STR_LNC
	},
	PASSPORT {
		override fun toString(): String = STR_PASSPORT
	};

	companion object {
		private const val STR_EGN: String = "EGN"
		private const val STR_EMBG = "EMBG"
		private const val STR_LNC = "LNCH"
		private const val STR_PASSPORT = "PASSPORT"

		private fun getPersonalNumberType() = when (BuildConfig.BUILD_TYPE) {
			BUILD_TYPE_MK -> STR_EMBG
			else -> STR_EGN
		}

		fun fromString(str: String?): LegitimationType =
			when (str) {
				STR_EGN, STR_EMBG -> PERSONAL_NUMBER
				STR_LNC -> FOREIGNER_NUMBER
				STR_PASSPORT -> PASSPORT
				else -> PERSONAL_NUMBER
			}
	}
}
