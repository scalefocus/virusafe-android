package bg.government.virusafe.app.registration

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.home.OnDialogButtonListener
import bg.government.virusafe.app.home.TermsAndConditionsDialog
import bg.government.virusafe.app.utils.FIELD_EMPTY_MSG
import bg.government.virusafe.app.utils.FIELD_INVALID_FORMAT_MSG
import bg.government.virusafe.app.utils.FIELD_LENGTH_ERROR_MSG
import bg.government.virusafe.app.utils.getPhoneNumberInputFilter
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

		with(binding.registrationTermsAndConditionsBtn) {
			paintFlags = this.paintFlags or Paint.UNDERLINE_TEXT_FLAG
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

		binding.registrationTermsAndConditionsBtn.setOnClickListener {
			if (!canClick()) {
				return@setOnClickListener
			}
			val dialog =
				TermsAndConditionsDialog.newInstance(!binding.termsAndConditionsCheckBox.isChecked)
			val fm: FragmentManager = activity?.supportFragmentManager ?: return@setOnClickListener
			dialog.setClickListener(this)
			dialog.show(fm, TermsAndConditionsDialog::class.java.canonicalName)
		}

		binding.termsAndConditionsCheckBox.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				setTermsAndConditionsColor(R.color.colorPrimary)
			}
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
		if (binding.termsAndConditionsCheckBox.isChecked) {
			setTermsAndConditionsColor(R.color.colorPrimary)
			showProgress()
			viewModel.register(binding.registrationPhoneNumberEt.text?.toString())
		} else {
			setTermsAndConditionsColor(R.color.color_red)
		}
	}

	private fun setTermsAndConditionsColor(color: Int) {
		binding.termsAndConditionsCheckBox.buttonTintList = ColorStateList.valueOf(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationIAgreeTxt.setTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
		binding.registrationTermsAndConditionsBtn.setTextColor(
			ContextCompat.getColor(
				activity ?: return, color
			)
		)
	}

	override fun onAgreeBtnClicked() {
		binding.termsAndConditionsCheckBox.isChecked = true
	}

	override fun getLayoutResId() = R.layout.fragment_registration

	override fun getViewModelClass() = RegistrationViewModel::class.java

	override fun getViewModelResId() = BR.registrationViewModel

	companion object {
		private const val DELAY = 300L
		const val PHONE_KEY = "bg.government.virusafe.app.registration.key1"
	}
}
