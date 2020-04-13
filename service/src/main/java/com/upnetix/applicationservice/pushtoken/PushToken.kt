package com.upnetix.applicationservice.pushtoken

import com.google.gson.annotations.SerializedName

data class PushToken(@SerializedName("pushToken") val pushToken: String) {

	override fun toString(): String {
		return ""
	}
}
