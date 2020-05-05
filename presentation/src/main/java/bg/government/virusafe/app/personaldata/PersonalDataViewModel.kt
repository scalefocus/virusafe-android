package bg.government.virusafe.app.personaldata

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import bg.government.virusafe.app.personaldata.PersonalDataFragment.Companion.CHECK_BOX_KEY
import bg.government.virusafe.app.utils.ComparableLiveData
import bg.government.virusafe.app.utils.FIELD_EMPTY_MSG
import bg.government.virusafe.app.utils.FIELD_INVALID_FORMAT_MSG
import bg.government.virusafe.app.utils.FIELD_LENGTH_ERROR_MSG
import bg.government.virusafe.app.utils.FOREIGNER_NUMBER
import bg.government.virusafe.app.utils.ID_NUMBER_HINT
import bg.government.virusafe.app.utils.INVALID_MIN_AGE_MSG
import bg.government.virusafe.app.utils.PASSPORT_HINT
import bg.government.virusafe.app.utils.SingleLiveEvent
import bg.government.virusafe.app.utils.USE_PERSONAL_DATA_TEXT_DISABLED
import bg.government.virusafe.app.utils.getPersonalIdValidator
import bg.government.virusafe.app.utils.hasValidForeignerNumberLength
import bg.government.virusafe.app.utils.hasValidPassportLength
import bg.government.virusafe.app.utils.hasValidPersonalIdLength
import bg.government.virusafe.app.utils.validators.LncValidator
import bg.government.virusafe.app.utils.validators.PersonalIdValidator
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.IRegistrationService
import com.upnetix.applicationservice.registration.model.Gender
import com.upnetix.applicationservice.registration.model.PersonalData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PersonalDataViewModel @Inject constructor(
	private val registrationService: IRegistrationService
) : AbstractViewModel() {

	val personalNumber: ObservableField<String> = ObservableField()
	val age: ObservableField<String> = ObservableField()
	val gender: MutableLiveData<Gender> = MutableLiveData(Gender.None)
	val healthStatus: ObservableField<String> = ObservableField()

	private val egnValidator: PersonalIdValidator = getPersonalIdValidator()
	private val lncValidator = LncValidator()
	private var oldPersonalData: PersonalData? = null

	private val _saveResponseData = SingleLiveEvent<ResponseWrapper<Unit>>()
	val saveResponseData: LiveData<ResponseWrapper<Unit>> = _saveResponseData

	private val _responseData = SingleLiveEvent<ResponseWrapper<PersonalData>>()
	val responseData: LiveData<ResponseWrapper<PersonalData>> = _responseData

	private val _errorIdData = ComparableLiveData<String?>()
	val errorIdData: LiveData<String?> = _errorIdData

	private val _errorAgeData = ComparableLiveData<String?>()
	val errorAgeData: LiveData<String?> = _errorAgeData

	private val personalNumberPropertyChangeCallback =
		object : Observable.OnPropertyChangedCallback() {
			override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
				if (_legitimationTypeSelected.value == LegitimationType.PERSONAL_NUMBER) {
					tryPrefillFields()
				}
				validatePersonalNumber()
			}
		}

	private val agePropertyChangeCallback =
		object : Observable.OnPropertyChangedCallback() {
			override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
				validateYears()
			}
		}

	private val _legitimationTypeSelected = MutableLiveData(LegitimationType.PERSONAL_NUMBER)
	val legitimationTypeSelected: LiveData<LegitimationType>
		get() = _legitimationTypeSelected

	val selectedLegitimationHint: LiveData<String> =
		Transformations.map(_legitimationTypeSelected) {
			when (it) {
				LegitimationType.PERSONAL_NUMBER -> ID_NUMBER_HINT
				LegitimationType.FOREIGNER_NUMBER -> FOREIGNER_NUMBER
				LegitimationType.PASSPORT -> PASSPORT_HINT
				else -> ""
			}
		}

	val componentsEditable: LiveData<Boolean> = Transformations.map(_legitimationTypeSelected) {
		it != LegitimationType.PERSONAL_NUMBER
	}

	var checkBoxVisibility: Int = View.INVISIBLE
		private set

	override fun receiveNavigationArgs(args: Bundle?) {
		super.receiveNavigationArgs(args)

		args ?: return

		checkBoxVisibility = if (args.getBoolean(CHECK_BOX_KEY))  View.VISIBLE else View.INVISIBLE
	}

	fun onLegitimationChange(legitimationType: LegitimationType) {
		if (this._legitimationTypeSelected.value == legitimationType) {
			return
		}
		this._legitimationTypeSelected.value = legitimationType
		egnValidator.clearAll()
		age.set(null)
		gender.value = Gender.fromString(null)
		personalNumber.set(null)
	}

	private fun tryPrefillFields() {
		egnValidator.initPersonalNumber(personalNumber.get())
		age.set(egnValidator.years?.toString())
		gender.value = Gender.fromString(egnValidator.gender)
	}

	override fun onCleared() {
		super.onCleared()
		personalNumber.removeOnPropertyChangedCallback(personalNumberPropertyChangeCallback)
		age.removeOnPropertyChangedCallback(agePropertyChangeCallback)
	}

	fun getData() {
		viewModelScope.launch {
			delay(DELAY)
			val personalDataResponse = registrationService.getPersonalData()
			if (personalDataResponse is ResponseWrapper.Success) {
				val response = personalDataResponse.response
				personalNumber.set(response.personalNumber)
				age.set(response.age?.toString())
				gender.value = Gender.fromString(response.gender)
				healthStatus.set(response.healthStatus)
				_legitimationTypeSelected.value =
					LegitimationType.fromString(
						response.identificationType
					)
				oldPersonalData = response
			}
			_responseData.postValue(personalDataResponse)
			personalNumber.addOnPropertyChangedCallback(personalNumberPropertyChangeCallback)
			age.addOnPropertyChangedCallback(agePropertyChangeCallback)
		}
	}

	fun sendData() {
		viewModelScope.launch {
			val personalDataResponse = if (oldPersonalData == getPersonalData()) {
				ResponseWrapper.Success(Unit)
			} else {
				registrationService.sendPersonalData(getPersonalData())
			}
			_saveResponseData.postValue(personalDataResponse)
		}
	}

	fun setGender(genderStr: String) {
		gender.value = Gender.fromString(genderStr)
	}

	private fun getPersonalData(): PersonalData = PersonalData(
		personalNumber = if (personalNumber.get()
				.isNullOrBlank()
		) null else personalNumber.get(),
		age = if (age.get().isNullOrBlank()) null else age.get()?.toInt(),
		gender = gender.value?.genderStr,
		healthStatus = if (healthStatus.get().isNullOrBlank()) null else healthStatus.get(),
		identificationType = _legitimationTypeSelected.value?.toString()
	)

	fun validatePersonalNumber() {
		when (_legitimationTypeSelected.value) {
			LegitimationType.PERSONAL_NUMBER -> validatePersonalId()
			LegitimationType.FOREIGNER_NUMBER -> validateForeignerData()
			LegitimationType.PASSPORT -> validatePassport()
		}
	}

	private fun validatePassport() {
		val passport = personalNumber.get()
		when {
			passport.isNullOrBlank() -> _errorIdData.postValue(localizeString(FIELD_EMPTY_MSG))
			hasValidPassportLength(passport).not() -> _errorIdData.postValue(localizeString(FIELD_LENGTH_ERROR_MSG))
			else -> _errorIdData.postValue(null)
		}
	}

	private fun validateForeignerData() {
		val foreignerNumber = personalNumber.get()
		when {
			foreignerNumber.isNullOrBlank() ->
				_errorIdData.postValue(localizeString(FIELD_EMPTY_MSG))
			hasValidForeignerNumberLength(foreignerNumber).not() ->
				_errorIdData.postValue(localizeString(FIELD_LENGTH_ERROR_MSG))
			lncValidator.isValid(foreignerNumber).not() ->
				_errorIdData.postValue(localizeString(FIELD_INVALID_FORMAT_MSG))
			else -> _errorIdData.postValue(null)
		}
	}

	private fun validatePersonalId() {
		val personalId = personalNumber.get()
		when {
			personalId.isNullOrBlank() -> _errorIdData.postValue(localizeString(FIELD_EMPTY_MSG))
			hasValidPersonalIdLength(personalId).not() -> _errorIdData.postValue(localizeString(FIELD_LENGTH_ERROR_MSG))
			egnValidator.isValidPersonalId().not() -> _errorIdData.postValue(localizeString(FIELD_INVALID_FORMAT_MSG))
			else -> _errorIdData.postValue(null)
		}
	}

	private fun validateYears() {
		val age = age.get() ?: ""
		if (age.isBlank().not()) {
			val ageInt = age.toIntOrNull() ?: 0
			if (ageInt < MIN_AGE) {
				_errorAgeData.postValue(localizeString(INVALID_MIN_AGE_MSG))
			} else {
				_errorAgeData.postValue(null)
			}
		} else {
			_errorAgeData.postValue(null)
		}
	}

	companion object {

		private const val DELAY = 400L
		private const val MIN_AGE = 14
	}
}
