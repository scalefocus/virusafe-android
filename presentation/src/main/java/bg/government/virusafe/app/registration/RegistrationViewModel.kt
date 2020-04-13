package bg.government.virusafe.app.registration

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import bg.government.virusafe.app.utils.SingleLiveEvent
import bg.government.virusafe.app.utils.getMinPhoneLength
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.IRegistrationService
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(private val registrationService: IRegistrationService) :
	AbstractViewModel() {

	private val _errorData =
		SingleLiveEvent<PhoneValidationErrors>()
	val errorData: LiveData<PhoneValidationErrors> = _errorData

	private val _pinData =
		SingleLiveEvent<ResponseWrapper<Unit>>()
	val pinData: LiveData<ResponseWrapper<Unit>> = _pinData

	fun register(phoneNumber: String?) {
		phoneNumber?.let {
			viewModelScope.launch {
				_pinData.postValue(registrationService.requestPin(it))
			}
		}
	}

	fun validatePhoneNumber(phoneNumber: String?) =
		if (phoneNumber.isNullOrEmpty() || phoneNumber.isNullOrBlank()) {
			_errorData.postValue(PhoneValidationErrors.EmptyError)
		} else if (phoneNumber.length < getMinPhoneLength()) {
			_errorData.postValue(PhoneValidationErrors.MinLengthError)
		} else if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
			_errorData.postValue(PhoneValidationErrors.InvalidFormatError)
		} else {
			_errorData.postValue(PhoneValidationErrors.NoError)
		}
}

sealed class PhoneValidationErrors {
	object NoError : PhoneValidationErrors()
	object EmptyError : PhoneValidationErrors()
	object MinLengthError : PhoneValidationErrors()
	object InvalidFormatError : PhoneValidationErrors()
}
