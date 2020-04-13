package bg.government.virusafe.app.selfcheck

import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import bg.government.virusafe.R
import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.location.LocationUpdateManager
import bg.government.virusafe.app.utils.NO_LABEL
import bg.government.virusafe.app.utils.OK_LABEL
import bg.government.virusafe.app.utils.WARNING_LABEL
import bg.government.virusafe.app.utils.WARNING_MSG
import bg.government.virusafe.app.utils.YES_LABEL
import bg.government.virusafe.app.utils.toLocationEntity
import bg.government.virusafe.databinding.FragmentSelfCheckBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import com.upnetix.applicationservice.geolocation.LocationEntity
import com.upnetix.applicationservice.registration.RegistrationServiceImpl
import com.upnetix.applicationservice.selfcheck.model.Question

class SelfCheckFragment : AbstractFragment<FragmentSelfCheckBinding, SelfCheckViewModel>(),
	QuestionClickListener {

	private val recyclerAdapter: SelfCheckAdapter = SelfCheckAdapter()

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)

		showProgress()
		binding.selfCheckRecycler.adapter = recyclerAdapter
		recyclerAdapter.setQuestionClickListener(this)
		recyclerAdapter.setTranslations(
			viewModel.localizeString(YES_LABEL),
			viewModel.localizeString(NO_LABEL)
		)

		binding.selfCheckSaveBtn.setOnClickListener {
			if (!canClick()) return@setOnClickListener
			if (!viewModel.areAllOfTheQuestionsAnswered()) {
				showFillSymptomsDialog()
			} else {
				val mainActivity = activity as? MainActivity
				mainActivity?.requestLocationTracking {
					showProgress()
					getLocationAndSave(layoutView)
				}
			}
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {

		viewModel.questionItemsData.observe(viewLifecycleOwner, Observer { responseWrapper ->
			hideProgress()
			processResponse(responseWrapper, { navigateBack() }) {
				recyclerAdapter.setItems(it)
			}
		})

		viewModel.sendQuestionnaireData.observe(viewLifecycleOwner, Observer { responseWrapper ->
			hideProgress()
			processResponse(responseWrapper) {
				if (navigatedFromRegistration) {
					sharedPrefsService.writeStringToSharedPrefs(
						RegistrationServiceImpl.FINISHED_REGISTRATION_KEY, REGISTRATION_YES
					)
					openFragmentFromRegistrationFlow(DoneFragment::class)
				} else {
					navigateToView(DoneFragment::class)
				}
			}
		})
	}

	private fun getLocationAndSave(layoutView: View) {
		LocationUpdateManager.getInstance(layoutView.context)
			.getLastKnownLocation(viewLifecycleOwner) {
				val locationEntity = it?.toLocationEntity() ?: LocationEntity()
				viewModel.onSaveBtnClicked(locationEntity)
			}
	}

	private fun showFillSymptomsDialog() {
		val dialog = FillSymptomsDialog.newInstance(
			viewModel.localizeString(WARNING_LABEL),
			viewModel.localizeString(WARNING_MSG),
			viewModel.localizeString(OK_LABEL)
		)
		val fm = activity?.supportFragmentManager ?: return
		dialog.show(fm, FillSymptomsDialog::class.java.canonicalName)
	}

	override fun onQuestionAnswered(question: Question) {
		viewModel.onQuestionChange(question)
	}

	override fun getLayoutResId() = R.layout.fragment_self_check

	override fun getViewModelClass() = SelfCheckViewModel::class.java

	override fun getViewModelResId() = BR.selfCheckViewModel

	companion object {
		private const val REGISTRATION_YES = "658"
	}
}
