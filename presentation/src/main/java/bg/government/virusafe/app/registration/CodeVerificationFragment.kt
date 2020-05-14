package bg.government.virusafe.app.registration

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService
import bg.government.virusafe.app.personaldata.PersonalDataFragment
import bg.government.virusafe.app.utils.FIELD_EMPTY_MSG
import bg.government.virusafe.app.utils.FIELD_INVALID_FORMAT_MSG
import bg.government.virusafe.app.utils.FIELD_LENGTH_ERROR_MSG
import bg.government.virusafe.app.utils.VERIFICATION_CODE_SEND_AGAIN
import bg.government.virusafe.databinding.FragmentCodeVerificationBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.upnetix.applicationservice.pushtoken.IPushTokenService
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.USE_PERSONAL_DATA_KEY
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CodeVerificationFragment :
	AbstractFragment<FragmentCodeVerificationBinding, CodeVerificationViewModel>() {

	@Inject
	lateinit var pushTokenService: IPushTokenService

	@SuppressLint("ClickableViewAccessibility")
	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)

		binding.codeVerificationContainer.setOnClickListener {
			hideKeyboard()
		}

		binding.codeVerificationCodeEt.setOnTouchListener { v, _ ->
			v.postDelayed(
				{ binding.codeVerificationScroll.fullScroll(View.FOCUS_DOWN) },
				DELAY
			)
			false
		}

		binding.codeVerificationCodeEt.addTextChangedListener {
			binding.codeVerificationCodeLayout.error = null
		}

		binding.codeVerificationBtn.setOnClickListener {
			if (canClick()) {
				viewModel.sendValidationCode(binding.codeVerificationCodeEt.text?.toString())
			}
		}

		binding.codeVerificationMissingBtn.setOnClickListener {
			if (canClick()) {
				showProgress()
				viewModel.resendCode()
			}
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {

		viewModel.errorData.observe(viewLifecycleOwner, Observer { error ->
			when (error) {
				is CodeValidationErrors.NoError -> {
					binding.codeVerificationCodeLayout.error = null
					showProgress()
				}

				CodeValidationErrors.EmptyError -> binding.codeVerificationCodeLayout.error =
					viewModel.localizeString(FIELD_EMPTY_MSG)
				CodeValidationErrors.MinLengthError -> binding.codeVerificationCodeLayout.error =
					viewModel.localizeString(FIELD_LENGTH_ERROR_MSG)
				CodeValidationErrors.InvalidFormatError -> binding.codeVerificationCodeLayout.error =
					viewModel.localizeString(FIELD_INVALID_FORMAT_MSG)
			}
		})

		viewModel.verifyData.observe(viewLifecycleOwner, Observer { responseWrapper ->
			hideProgress()
			processResponse(responseWrapper) {
				trySendingFirebaseToken()

				openFragmentFromRegistrationFlow(PersonalDataFragment::class)
			}
		})

		viewModel.resendCodeData.observe(viewLifecycleOwner, Observer { responseWrapper ->
			hideProgress()
			processResponse(responseWrapper) {
				Toast.makeText(
					activity,
					viewModel.localizeString(VERIFICATION_CODE_SEND_AGAIN),
					Toast.LENGTH_SHORT
				).show()
			}
		})
	}

	override fun getLayoutResId() = R.layout.fragment_code_verification

	override fun getViewModelClass() = CodeVerificationViewModel::class.java

	override fun getViewModelResId() = BR.codeVerificationViewModel

	private fun trySendingFirebaseToken() {
		val firebaseToken =
			sharedPrefsService.readStringFromSharedPrefs(FirebaseCloudMessagingService.FIREBASE_TOKEN)
		if (firebaseToken.isNotBlank()) {
			sharedPrefsService.clearValue(FirebaseCloudMessagingService.FIREBASE_TOKEN)
			GlobalScope.launch {
				pushTokenService.sendPushToken(firebaseToken)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			SMS_RETRIEVE_STATUS_CODE -> {
				if ((resultCode == Activity.RESULT_OK) && (data != null)) {
					val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
					message?.let {
						val code = it.replace("\\D+".toRegex(), "")
						binding.codeVerificationCodeEt.setText(code)
					}
				}
			}
		}
	}

	companion object {
		private const val DELAY = 300L

		const val SMS_RETRIEVE_STATUS_CODE = 222
	}
}
