package com.upnetix.applicationservice.registration.model

import com.google.gson.annotations.SerializedName

data class TokenRefreshRequest(
	@SerializedName("refreshToken")
	val refreshToken: String
) {

	override fun toString(): String {
		return ""
	}
}
