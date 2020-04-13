package bg.government.virusafe.app.selfcheck

import android.util.SparseArray
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bg.government.virusafe.app.utils.values
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.geolocation.Location
import com.upnetix.applicationservice.geolocation.LocationEntity
import com.upnetix.applicationservice.selfcheck.ISelfCheckService
import com.upnetix.applicationservice.selfcheck.model.Answer
import com.upnetix.applicationservice.selfcheck.model.Question
import com.upnetix.applicationservice.selfcheck.model.QuestionnaireRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class SelfCheckViewModel @Inject constructor(private val selfCheckService: ISelfCheckService) :
	AbstractViewModel() {

	private val questionsMap: SparseArray<Question> = SparseArray()
	private val questionItemsList = mutableListOf<Question>()

	val isNoSymptomsChecked: ObservableBoolean = ObservableBoolean(false)

	private val _questionItemsData = MutableLiveData<ResponseWrapper<List<Question>>>()
	val questionItemsData: LiveData<ResponseWrapper<List<Question>>> = _questionItemsData

	private val _sendQuestionnaireData = MutableLiveData<ResponseWrapper<Unit>>()
	val sendQuestionnaireData: LiveData<ResponseWrapper<Unit>> = _sendQuestionnaireData

	init {
		viewModelScope.launch {
			delay(DELAY)
			val questionsResponse = selfCheckService.getQuestions()
			if (questionsResponse is ResponseWrapper.Success) {
				questionItemsList.addAll(questionsResponse.response)
			}
			_questionItemsData.postValue(questionsResponse)
		}
	}

	fun onNoSymptomsClick() {

		var answer: Boolean? = null
		if (isNoSymptomsChecked.get()) {
			answer = false
		} else {
			questionsMap.clear()
		}

		questionItemsList.forEach { question ->
			question.answer = answer
			if (answer != null) {
				questionsMap.put(question.id, question)
			}
		}
		_questionItemsData.postValue(ResponseWrapper.Success(questionItemsList))
	}

	fun onSaveBtnClicked(locationEntity: LocationEntity) {
		if (!areAllOfTheQuestionsAnswered()) {
			return
		}

		viewModelScope.launch {
			_sendQuestionnaireData.postValue(
				selfCheckService.sendQuestionnaire(prepareQuestionnaireData(locationEntity))
			)
		}
	}

	private fun prepareQuestionnaireData(locationEntity: LocationEntity): QuestionnaireRequest {
		val answers = questionsMap.values().map {
			Answer(it.id, it.answer?.toString() ?: "null")
		}
		return QuestionnaireRequest(
			Location(locationEntity.lat, locationEntity.lng),
			Date().time,
			answers
		)
	}

	fun onQuestionChange(question: Question) {
		if (question.answer == true) {
			isNoSymptomsChecked.set(false)
		}
		questionsMap.put(question.id, question)

		if (areAllOfTheQuestionsAnswered()) {
			isNoSymptomsChecked.set(areAllOfTheQuestionsAnsweredFalse())
		}
	}

	fun areAllOfTheQuestionsAnswered(): Boolean =
		questionItemsList.size == questionsMap.size()

	private fun areAllOfTheQuestionsAnsweredFalse(): Boolean {
		for (i in 0 until questionsMap.size()) {
			val key: Int = questionsMap.keyAt(i)
			val question = questionsMap.get(key)
			if (question.answer == true) {
				return false
			}
		}
		return true
	}

	companion object {
		private const val DELAY = 400L
	}
}
