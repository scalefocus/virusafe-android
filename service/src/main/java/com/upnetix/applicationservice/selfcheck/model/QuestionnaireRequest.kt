package com.upnetix.applicationservice.selfcheck.model

import com.google.gson.annotations.SerializedName
import com.upnetix.applicationservice.geolocation.Location
import java.io.Serializable

data class QuestionnaireRequest(
	@SerializedName("location")
	val location: Location,
	@SerializedName("timestamp")
	val timestamp: Long,
	@SerializedName("answers")
	val answers: List<Answer>
) : Serializable {

	override fun toString(): String {
		return ""
	}
}

data class Answer(
	@SerializedName("questionId")
	val questionId: Int,
	@SerializedName("answer")
	val answer: String
) : Serializable {

	override fun toString(): String {
		return ""
	}
}
