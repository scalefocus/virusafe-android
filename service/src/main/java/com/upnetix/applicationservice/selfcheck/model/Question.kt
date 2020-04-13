package com.upnetix.applicationservice.selfcheck.model

import com.google.gson.annotations.SerializedName

data class Question(
	@SerializedName("id")
	val id: Int,
	@SerializedName("questionTitle")
	val questionTitle: String,
	@SerializedName("answer")
	var answer: Boolean?
) {

	override fun toString(): String {
		return ""
	}
}
