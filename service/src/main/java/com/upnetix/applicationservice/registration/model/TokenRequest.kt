package com.upnetix.applicationservice.registration.model

import com.google.gson.annotations.SerializedName

data class TokenRequest(
	@SerializedName("phoneNumber")
	val phoneNumber: String,
	@SerializedName("pin")
	val pin: String
) {

	override fun toString(): String {
		return ""
	}
}
