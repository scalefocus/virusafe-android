package bg.government.virusafe.mvvm.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.home.Agreement
import bg.government.virusafe.app.home.AgreementsDialog
import bg.government.virusafe.app.home.OnDialogButtonListener
import bg.government.virusafe.app.personaldata.PersonalDataFragment
import bg.government.virusafe.app.registration.RegistrationFragment
import bg.government.virusafe.app.selfcheck.SelfCheckFragment
import bg.government.virusafe.app.utils.AND_LABEL
import bg.government.virusafe.app.utils.ERROR_OCCURRED_LABEL
import bg.government.virusafe.app.utils.FIELD_INVALID_FORMAT_MSG
import bg.government.virusafe.app.utils.GENERIC_ERROR_MSG
import bg.government.virusafe.app.utils.HOURS_LABEL
import bg.government.virusafe.app.utils.HOUR_LABEL
import bg.government.virusafe.app.utils.INVALID_AGE_MSG
import bg.government.virusafe.app.utils.INVALID_PHONE_MSG
import bg.government.virusafe.app.utils.INVALID_PIN_MSG
import bg.government.virusafe.app.utils.INVALID_PRE_EXISTING_CONDITIONS_MSG
import bg.government.virusafe.app.utils.MINUTES_LABEL
import bg.government.virusafe.app.utils.MINUTE_LABEL
import bg.government.virusafe.app.utils.NO_INTERNET_MSG
import bg.government.virusafe.app.utils.OK_LABEL
import bg.government.virusafe.app.utils.REDIRECT_TO_REGISTRATION_MSG
import bg.government.virusafe.app.utils.SELF_CHECK_TOO_MANY_REQUESTS_MSG
import bg.government.virusafe.app.utils.SMTH_WENT_WRONG
import bg.government.virusafe.app.utils.TOO_MANY_REQUESTS_MSG
import bg.government.virusafe.mvvm.activity.AbstractActivity
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.applicationservice.base.BaseService.Companion.INVALID_PIN
import com.upnetix.applicationservice.base.BaseService.Companion.INVALID_TOKEN
import com.upnetix.applicationservice.base.BaseService.Companion.PRECONDITION_FAILED
import com.upnetix.applicationservice.base.BaseService.Companion.SERVER_ERROR
import com.upnetix.applicationservice.base.BaseService.Companion.TOO_MANY_REQUESTS
import com.upnetix.applicationservice.base.ResponseWrapper
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.HAS_REGISTRATION_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.NEW_ACCESS_TOKEN_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.OLD_TOKEN_KEY
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.REFRESH_TOKEN_KEY
import com.upnetix.presentation.view.BaseFragment
import com.upnetix.presentation.view.IView
import com.upnetix.service.sharedprefs.ISharedPrefsService
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Abstract class for creating fragments.
 *
 * @param B  the fragment binding type
 * @param VM the view model
 *
 * @author stoyan.yanev
 */
abstract class AbstractFragment<B : ViewDataBinding, VM : AbstractViewModel> :
	BaseFragment<B, VM>() {

	@Inject
	protected lateinit var sharedPrefsService: ISharedPrefsService

	var navigatedFromRegistration = false
		private set

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		checkNavigation()
		val view = super.onCreateView(inflater, container, savedInstanceState)
		addViewModelObservers(viewLifecycleOwner)
		return view
	}

	/**
	 * Used to attach observers to the viewModel live data
	 * Called in {@link BaseFragment#onViewCreated(View, Bundle)}
	 *
	 * @param viewLifecycleOwner The {@link LifecycleOwner} of the view for this fragment, @see #getViewLifecycleOwner()
	 */
	abstract fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner)

	@CallSuper
	override fun onBack(): Boolean {
		hideProgress()
		return super.onBack()
	}

	protected fun showProgress() {
		(activity as? AbstractActivity<*, *>)?.showProgress()
	}

	protected fun hideProgress() {
		(activity as? AbstractActivity<*, *>)?.hideProgress()
	}

	protected fun canClick(): Boolean {
		val currentActivity = activity as? AbstractActivity<*, *>
		return currentActivity?.canClick() ?: true
	}

	protected fun <T : IView<*>> openFragmentFromRegistrationFlow(
		clazz: KClass<T>,
		bundle: Bundle? = null
	) {
		val newBundle = Bundle().apply {
			putBoolean(NAVIGATED_FROM_REGISTRATION, true)
			bundle?.let {
				putAll(it)
			}
		}
		navigateToView(clazz, newBundle)
	}

	private fun checkNavigation() {
		arguments?.let { args ->
			if (args.getBoolean(NAVIGATED_FROM_REGISTRATION)) {
				navigatedFromRegistration = true
				args.remove(NAVIGATED_FROM_REGISTRATION)
			}
		}
	}

	protected fun <T> processResponse(
		responseWrapper: ResponseWrapper<T>,
		onErrorDialogDismiss: (() -> Unit)? = null,
		onSuccess: ((response: T) -> Unit)? = null
	) {
		when (responseWrapper) {
			is ResponseWrapper.Success ->
				onSuccess?.invoke(responseWrapper.response)
			is ResponseWrapper.NoInternetError ->
				onNoInternetError(onErrorDialogDismiss)
			is ResponseWrapper.Error ->
				onError(responseWrapper, onErrorDialogDismiss)
		}
	}

	private fun onNoInternetError(onErrorDialogDismiss: (() -> Unit)?) {
		showErrorDialog(
			message = viewModel.localizeString(NO_INTERNET_MSG),
			onErrorDialogDismiss = onErrorDialogDismiss
		)
	}

	private fun onError(error: ResponseWrapper.Error, onErrorDialogDismiss: (() -> Unit)? = null) {
		when (val code = error.code) {
			INVALID_TOKEN -> showErrorDialog(code = code) {
				startRegistration()
			}
			TOO_MANY_REQUESTS -> showErrorDialog(
				param = getTimeLeft(error.response?.message?.toLong() ?: 0),
				code = code,
				onErrorDialogDismiss = onErrorDialogDismiss
			)
			PRECONDITION_FAILED -> showErrorDialog(
				param = error.response?.validationErrors?.firstOrNull()?.fieldName,
				code = code,
				onErrorDialogDismiss = onErrorDialogDismiss
			)
			else -> showErrorDialog(code = code, onErrorDialogDismiss = onErrorDialogDismiss)
		}
	}

	private fun startRegistration() {
		sharedPrefsService.clearValue(HAS_REGISTRATION_KEY)
		sharedPrefsService.clearValue(OLD_TOKEN_KEY)
		sharedPrefsService.clearValue(NEW_ACCESS_TOKEN_KEY)
		sharedPrefsService.clearValue(REFRESH_TOKEN_KEY)

		val intent = Intent(activity, MainActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		startActivity(intent)
	}

	protected fun showErrorDialog(
		message: String? = null,
		param: String? = null,
		code: Int? = null,
		onErrorDialogDismiss: (() -> Unit)? = null
	) {
		context?.let {
			val builder = AlertDialog.Builder(it)
			builder.setTitle(getCodeTitle(code))
			builder.setPositiveButton(viewModel.localizeString(OK_LABEL), null)
			if (code != null) {
				builder.setMessage(getCodeMsg(code, param))
			} else {
				builder.setMessage(message ?: viewModel.localizeString(GENERIC_ERROR_MSG))
			}
			builder.setOnDismissListener { onErrorDialogDismiss?.invoke() }
			builder.show()
		}
	}

	private fun getCodeTitle(code: Int?): String = when (code) {
		INVALID_TOKEN -> EMPTY_STR
		TOO_MANY_REQUESTS -> EMPTY_STR
		PRECONDITION_FAILED -> EMPTY_STR
		INVALID_PIN -> EMPTY_STR
		else -> viewModel.localizeString(ERROR_OCCURRED_LABEL)
	}

	private fun getCodeMsg(code: Int, param: String? = null): String =
		when (code) {
			SERVER_ERROR -> viewModel.localizeString(SMTH_WENT_WRONG)
			INVALID_PIN -> viewModel.localizeString(INVALID_PIN_MSG)
			INVALID_TOKEN -> viewModel.localizeString(REDIRECT_TO_REGISTRATION_MSG)
			TOO_MANY_REQUESTS -> getTooManyRequestsMsg(param)
			PRECONDITION_FAILED -> getPreconditionFailedMsg(param)
			else -> viewModel.localizeString(GENERIC_ERROR_MSG)
		}

	private fun getTooManyRequestsMsg(param: String?): String {
		val strValue = when (this) {
			is SelfCheckFragment ->
				viewModel.localizeString(SELF_CHECK_TOO_MANY_REQUESTS_MSG)
			else ->
				viewModel.localizeString(TOO_MANY_REQUESTS_MSG)
		}
		return String.format(strValue, param)
	}

	private fun getPreconditionFailedMsg(param: String? = null): String {
		val key = when (this) {
			is RegistrationFragment -> INVALID_PHONE_MSG
			is PersonalDataFragment -> getPersonaDataInvalidMsg(param)
			else -> FIELD_INVALID_FORMAT_MSG
		}
		return viewModel.localizeString(key)
	}

	private fun getPersonaDataInvalidMsg(param: String?): String = when (param) {
		FIELD_AGE -> INVALID_AGE_MSG
		FIELD_PRE_EXISTING_CONDITIONS -> INVALID_PRE_EXISTING_CONDITIONS_MSG
		else -> FIELD_INVALID_FORMAT_MSG
	}

	private fun getTimeLeft(timeLeft: Long): String {
		var hours = timeLeft / SECONDS_IN_HOUR
		var minutes = (timeLeft % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
		val seconds = timeLeft % SECONDS_IN_MINUTE

		if (seconds != ZERO) {
			minutes += ONE
		}

		if (minutes == SECONDS_IN_MINUTE) {
			hours += ONE
			minutes = ZERO
		}

		val hourStr =
			if (hours != ONE) viewModel.localizeString(HOURS_LABEL) else viewModel.localizeString(
				HOUR_LABEL
			)
		val minuteLabel =
			if (minutes != ONE) viewModel.localizeString(MINUTES_LABEL) else viewModel.localizeString(
				MINUTE_LABEL
			)

		return if (hours != ZERO) {
			val andLabel = viewModel.localizeString(AND_LABEL)
			val minutesFormat = if (minutes != ZERO) "$andLabel $minutes $minuteLabel" else ""

			"$hours $hourStr $minutesFormat"
		} else {
			"$minutes $minuteLabel"
		}
	}

	protected fun showAgreementsDialog(
		title: String,
		description: String,
		agreement: Agreement,
		showAgreeBtn: Boolean = false,
		onClick: OnDialogButtonListener? = null
	) {
		activity?.supportFragmentManager?.let {
			val dialog = AgreementsDialog.newInstance(title, description, agreement, showAgreeBtn)
			dialog.setClickListener(onClick)
			dialog.show(it, AgreementsDialog::class.java.canonicalName)
		}
	}

	companion object {
		const val NAVIGATED_FROM_REGISTRATION = "nav_key_1"

		private const val EMPTY_STR = ""
		private const val FIELD_AGE = "age"
		private const val FIELD_PRE_EXISTING_CONDITIONS = "preExistingConditions"

		private const val ZERO = 0L
		private const val ONE = 1L
		private const val SECONDS_IN_MINUTE = 60L
		private const val SECONDS_IN_HOUR = 3600L
	}
}
