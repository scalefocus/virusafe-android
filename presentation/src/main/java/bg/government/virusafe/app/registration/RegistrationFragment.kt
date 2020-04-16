package bg.government.virusafe.app.registration

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.home.Agreement
import bg.government.virusafe.app.home.OnDialogButtonListener
import bg.government.virusafe.app.utils.DATA_PROTECTION_NOTICE_SMALL_LBL
import bg.government.virusafe.app.utils.DPN_DESCRIPTION
import bg.government.virusafe.app.utils.DPN_TITLE
import bg.government.virusafe.app.utils.FIELD_EMPTY_MSG
import bg.government.virusafe.app.utils.FIELD_INVALID_FORMAT_MSG
import bg.government.virusafe.app.utils.FIELD_LENGTH_ERROR_MSG
import bg.government.virusafe.app.utils.I_AGREE_WITH_LBL
import bg.government.virusafe.app.utils.I_CONSENT_TO_LBL
import bg.government.virusafe.app.utils.TERMS_N_CONDITIONS_SMALL_LBL
import bg.government.virusafe.app.utils.TNC_PART_ONE
import bg.government.virusafe.app.utils.TNC_PART_TWO
import bg.government.virusafe.app.utils.TNC_TITLE
import bg.government.virusafe.app.utils.getPhoneNumberInputFilter
import bg.government.virusafe.app.utils.setClickablePhrase
import bg.government.virusafe.databinding.FragmentRegistrationBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment

class RegistrationFragment :
	AbstractFragment<FragmentRegistrationBinding, RegistrationViewModel>(), OnDialogButtonListener {

	@SuppressLint("ClickableViewAccessibility")
	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)

		binding.registrationPhoneNumberEt.filters = getPhoneNumberInputFilter()

		binding.registrationContainer.setOnClickListener {
			hideKeyboard()
		}

		binding.registrationPhoneNumberEt.setOnTouchListener { v, _ ->
			with(binding.registrationContainer) {
				if (isFocusable) {
					this.isFocusable = false
					this.isFocusableInTouchMode = false
				}
			}

			v.postDelayed({ binding.registrationScroll.fullScroll(View.FOCUS_DOWN) }, DELAY)
			false
		}

		binding.registrationPhoneNumberEt.addTextChangedListener {
			binding.registrationPhoneNumberLayout.error = null
		}

		binding.registrationBtn.setOnClickListener {
			if (canClick()) {
				viewModel.validatePhoneNumber(binding.registrationPhoneNumberEt.text?.toString())
			}
		}

		binding.termsAndConditionsCheckBox.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				setTermsAndConditionsColor(R.color.colorPrimary)
			}
		}

		binding.registrationTermsAndConditionsTxt.setClickablePhrase(
			fullText = "${viewModel.localizeString(I_AGREE_WITH_LBL)} " +
					viewModel.localizeString(TERMS_N_CONDITIONS_SMALL_LBL),
			clickablePhrase = viewModel.localizeString(TERMS_N_CONDITIONS_SMALL_LBL),
			shouldBoldPhrase = false,
			shouldUnderlinePhrase = true
		) {
			if (!canClick()) {
				return@setClickablePhrase
			}
			showAgreementsDialog(
				viewModel.localizeString(TNC_TITLE),
				viewModel.localizeString(TNC_PART_ONE) + viewModel.localizeString(TNC_PART_TWO),
				Agreement.TermsAndConditions,
				!binding.termsAndConditionsCheckBox.isChecked,
				this
			)
		}

		binding.dataProtectionNoticeCheckBox.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				setDataProtectionNoticeColor(R.color.colorPrimary)
			}
		}

		binding.registrationDataProtectionNoticeTxt.setClickablePhrase(
			fullText = "${viewModel.localizeString(I_CONSENT_TO_LBL)} " +
					viewModel.localizeString(DATA_PROTECTION_NOTICE_SMALL_LBL),
			clickablePhrase = viewModel.localizeString(DATA_PROTECTION_NOTICE_SMALL_LBL),
			shouldBoldPhrase = false,
			shouldUnderlinePhrase = true
		) {
			if (!canClick()) {
				return@setClickablePhrase
			}
			showAgreementsDialog(
				viewModel.localizeString(DPN_TITLE),
				viewModel.localizeString(DPN_DESCRIPTION),
				Agreement.DataProtectionNotice,
				!binding.dataProtectionNoticeCheckBox.isChecked,
				this
			)
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {

		viewModel.errorData.observe(viewLifecycleOwner, Observer { error ->
			when (error) {
				is PhoneValidationErrors.NoError -> onNoError()
				PhoneValidationErrors.EmptyError -> binding.registrationPhoneNumberLayout.error =
					viewModel.localizeString(FIELD_EMPTY_MSG)
				PhoneValidationErrors.MinLengthError -> binding.registrationPhoneNumberLayout.error =
					viewModel.localizeString(FIELD_LENGTH_ERROR_MSG)
				PhoneValidationErrors.InvalidFormatError -> binding.registrationPhoneNumberLayout.error =
					viewModel.localizeString(FIELD_INVALID_FORMAT_MSG)
			}
		})

		viewModel.pinData.observe(viewLifecycleOwner, Observer { responseWrapper ->
			hideProgress()
			processResponse(responseWrapper) {
				navigateToView(
					CodeVerificationFragment::class,
					Bundle().apply {
						this.putString(
							PHONE_KEY,
							binding.registrationPhoneNumberEt.text?.toString()
						)
					}
				)
			}
		})
	}

	private fun onNoError() {
		binding.registrationPhoneNumberLayout.error = null
		if (binding.termsAndConditionsCheckBox.isChecked && binding.dataProtectionNoticeCheckBox.isChecked) {
			setTermsAndConditionsColor(R.color.colorPrimary)
			setDataProtectionNoticeColor(R.color.colorPrimary)
			showProgress()
			viewModel.register(binding.registrationPhoneNumberEt.text?.toString())
		} else {
			if (!binding.termsAndConditionsCheckBox.isChecked)
				setTermsAndConditionsColor(R.color.color_red)
			if (!binding.dataProtectionNoticeCheckBox.isChecked)
				setDataProtectionNoticeColor(R.color.color_red)
		}
	}

	private fun setTermsAndConditionsColor(color: Int) {
		binding.termsAndConditionsCheckBox.buttonTintList = ColorStateList.valueOf(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationTermsAndConditionsTxt.setTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationTermsAndConditionsTxt.setLinkTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
	}

	private fun setDataProtectionNoticeColor(color: Int) {
		binding.dataProtectionNoticeCheckBox.buttonTintList = ColorStateList.valueOf(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationDataProtectionNoticeTxt.setTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationDataProtectionNoticeTxt.setLinkTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
	}

	override fun onAgreeBtnClicked(agreement: Agreement) {
		when (agreement) {
			Agreement.TermsAndConditions -> binding.termsAndConditionsCheckBox.isChecked = true
			Agreement.DataProtectionNotice -> binding.dataProtectionNoticeCheckBox.isChecked = true
		}
	}

	override fun getLayoutResId() = R.layout.fragment_registration

	override fun getViewModelClass() = RegistrationViewModel::class.java

	override fun getViewModelResId() = BR.registrationViewModel

	companion object {
		private const val DELAY = 300L
		const val PHONE_KEY = "bg.government.virusafe.app.registration.key1"
	}
}
