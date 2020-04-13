package bg.government.virusafe.app.home

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import bg.government.virusafe.R
import bg.government.virusafe.app.utils.I_AGREE_LABEL
import bg.government.virusafe.app.utils.TNC_PART_ONE
import bg.government.virusafe.app.utils.TNC_PART_TWO
import bg.government.virusafe.app.utils.TNC_TITLE
import bg.government.virusafe.databinding.FragmentTermsAndConditionsBinding
import bg.government.virusafe.mvvm.dialog.AbstractDialogFragment
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel
import com.upnetix.presentation.view.DEFAULT_VIEW_MODEL_ID

class TermsAndConditionsDialog :
	AbstractDialogFragment<FragmentTermsAndConditionsBinding, EmptyViewModel>() {

	private var agreeButtonListener: OnDialogButtonListener? = null

	companion object {
		private const val SHOW_AGREE_BTN = "show_agree_btn"
		internal fun newInstance(showAgreeBtn: Boolean) = TermsAndConditionsDialog()
			.apply {
				arguments = Bundle().apply {
					putBoolean(SHOW_AGREE_BTN, showAgreeBtn)
				}
			}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NO_FRAME, R.style.AppTheme)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setAnimation(R.style.DialogAnimation)
	}

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)
		val title = viewModel.localizeString(TNC_TITLE)
		val description =
			viewModel.localizeString(TNC_PART_ONE) + viewModel.localizeString(TNC_PART_TWO)
		binding.termsAndConditionsTitle.text = title
		binding.termsAndConditionsTxt.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
		} else {
			Html.fromHtml(description)
		}
		binding.termsAndConditionsTxt.movementMethod = LinkMovementMethod.getInstance();

		val showAgreeBtn = arguments?.getBoolean(SHOW_AGREE_BTN) ?: false
		if (showAgreeBtn) {
			binding.agreeBtn.text = viewModel.localizeString(I_AGREE_LABEL)
			binding.agreeBtn.visibility = View.VISIBLE
			binding.agreeBtn.setOnClickListener {
				setAnimation(R.anim.exit_to_bottom)
				dismiss()
				agreeButtonListener?.onAgreeBtnClicked()
			}
		}
	}

	fun setClickListener(listener: OnDialogButtonListener) {
		agreeButtonListener = listener
	}

	override fun getLayoutResId() = R.layout.fragment_terms_and_conditions

	override fun getViewModelClass() = EmptyViewModel::class.java

	override fun getViewModelResId() = DEFAULT_VIEW_MODEL_ID
}

interface OnDialogButtonListener {
	fun onAgreeBtnClicked()
}
