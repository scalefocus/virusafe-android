package com.upnetix.applicationservice.registration.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
	@SerializedName("accessToken")
	val accessToken: String
) {

	override fun toString(): String {
		return ""
	}
}
