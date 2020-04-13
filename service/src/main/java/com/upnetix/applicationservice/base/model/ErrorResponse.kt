package com.upnetix.applicationservice.base.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
	@SerializedName("message")
	val message: String,
	@SerializedName("validationErrors")
	val validationErrors: List<ValidationError>
) {

	override fun toString(): String {
		return ""
	}
}

data class ValidationError(
	@SerializedName("fieldName")
	val fieldName: String,
	@SerializedName("validationMessage")
	val validationMsg: String
) {

	override fun toString(): String {
		return ""
	}
}
