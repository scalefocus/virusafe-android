package com.upnetix.applicationservice.base

import android.content.Context
import com.google.gson.Gson
import com.upnetix.applicationservice.base.model.ErrorResponse
import com.upnetix.service.util.NetworkConnectionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ProtocolException

abstract class BaseService(private var ctx: Context) {

	@Suppress("TooGenericExceptionCaught")
	suspend fun <T> executeRetrofitCall(apiCall: suspend () -> T): ResponseWrapper<T> =
		withContext(Dispatchers.IO) {
			try {
				ResponseWrapper.Success(apiCall.invoke())
			} catch (e: HttpException) {
				val errorBodyStr = e.response()?.errorBody()?.string()
				val response =
					parseError(errorBodyStr, ErrorResponse::class.java)
				ResponseWrapper.Error(response, e.code())
			} catch (e: AuthProtocolException) {
				ResponseWrapper.Error(code = e.code)
			} catch (e: Exception) {
				if (!NetworkConnectionUtil.hasNetworkConnection(ctx))
					ResponseWrapper.NoInternetError
				else
					ResponseWrapper.Error(code = BAD_REQUEST)
			}
		}

	private fun <T> parseError(errorStr: String?, clazz: Class<T>): T? {
		return try {
			Gson().fromJson(errorStr, clazz)
		} catch (ex: java.lang.Exception) {
			null
		}
	}

	companion object {
		const val BAD_REQUEST = 400
		const val INVALID_TOKEN = 403
		const val TOO_MANY_REQUESTS = 429
		const val INVALID_PIN = 438
		const val PRECONDITION_FAILED = 412
		const val SERVER_ERROR = 500
		const val KEY_VALUE = "value"
	}
}

class AuthProtocolException constructor(val code: Int) : ProtocolException()
