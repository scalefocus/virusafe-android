package com.upnetix.applicationservice.base

import com.upnetix.applicationservice.base.model.ErrorResponse

sealed class ResponseWrapper<out T> {
	data class Success<out T>(val response: T) : ResponseWrapper<T>()
	data class Error(val response: ErrorResponse? = null, val code: Int) :
		ResponseWrapper<Nothing>()

	object NoInternetError : ResponseWrapper<Nothing>()
}
