package com.upnetix.applicationservice.registration.model

import com.google.gson.annotations.SerializedName

data class PersonalData(
	@SerializedName("identificationNumber")
	val personalNumber: String?,
	@SerializedName("age")
	val age: Int?,
	@SerializedName("gender")
	val gender: String?,
	@SerializedName("preExistingConditions")
	val healthStatus: String?,
	@SerializedName("identificationType")
	val identificationType: String?
) {

	override fun toString(): String {
		return ""
	}
}

sealed class Gender(val genderStr: String?) {
	object Male : Gender(VALUE_MALE)
	object Female : Gender(VALUE_FEMALE)
	object None : Gender(null)

	override fun toString(): String {
		return ""
	}

	companion object {

		const val VALUE_MALE = "MALE"
		const val VALUE_FEMALE = "FEMALE"

		fun fromString(genderStr: String?): Gender =
			when (genderStr) {
				VALUE_MALE -> Male
				VALUE_FEMALE -> Female
				else -> None
			}
	}
}
