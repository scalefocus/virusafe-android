package bg.government.virusafe.app.registration

import android.os.Bundle
import android.util.Patterns
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bg.government.virusafe.app.registration.RegistrationFragment.Companion.PHONE_KEY
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.IRegistrationService
import com.upnetix.applicationservice.registration.model.TokenResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class CodeVerificationViewModel @Inject constructor(private val registrationService: IRegistrationService) :
	AbstractViewModel() {

	private val _errorData = MutableLiveData<CodeValidationErrors>()
	val errorData: LiveData<CodeValidationErrors> = _errorData

	private val _verifyData = MutableLiveData<ResponseWrapper<TokenResponse>>()
	val verifyData: LiveData<ResponseWrapper<TokenResponse>> = _verifyData

	private val _resendCode = MutableLiveData<ResponseWrapper<Unit>>()
	val resendCodeData: LiveData<ResponseWrapper<Unit>> = _resendCode

	val phoneNumber: ObservableField<String> = ObservableField()

	override fun receiveNavigationArgs(args: Bundle?) {
		super.receiveNavigationArgs(args)
		args?.let {
			phoneNumber.set(args.getString(PHONE_KEY))
		}
	}

	fun sendValidationCode(validationCode: String?) {
		if (!validateCode(validationCode)) {
			return
		}

		validationCode?.let { pin ->
			phoneNumber.get()?.let { phone ->
				viewModelScope.launch {
					_verifyData.postValue(registrationService.verifyPin(phone, pin))
				}
			}
		}
	}

	fun resendCode() {
		phoneNumber.get()?.let { phone ->
			viewModelScope.launch {
				_resendCode.postValue(registrationService.requestPin(phone))
			}
		}
	}

	private fun validateCode(code: String?): Boolean =
		if (code.isNullOrEmpty() || code.isNullOrBlank()) {
			_errorData.postValue(CodeValidationErrors.EmptyError)
			false
		} else if (code.length < MIN_LENGTH) {
			_errorData.postValue(CodeValidationErrors.MinLengthError)
			false
		} else if (!Patterns.PHONE.matcher(code).matches()) {
			_errorData.postValue(CodeValidationErrors.InvalidFormatError)
			false
		} else {
			_errorData.postValue(CodeValidationErrors.NoError)
			true
		}

	companion object {
		private const val MIN_LENGTH = 6
	}
}

sealed class CodeValidationErrors {
	object NoError : CodeValidationErrors()
	object EmptyError : CodeValidationErrors()
	object MinLengthError : CodeValidationErrors()
	object InvalidFormatError : CodeValidationErrors()
}
