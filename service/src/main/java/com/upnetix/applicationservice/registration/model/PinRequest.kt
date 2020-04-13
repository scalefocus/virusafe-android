package com.upnetix.applicationservice.registration.model

import com.google.gson.annotations.SerializedName

data class PinRequest(
	@SerializedName("phoneNumber")
	val phoneNumber: String
) {

	override fun toString(): String {
		return ""
	}
}
