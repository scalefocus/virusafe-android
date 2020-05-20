package bg.government.virusafe.app.localization

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.appinfo.AppInfoFragment
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService
import bg.government.virusafe.app.utils.URL_VIRUSAFE_WHY
import bg.government.virusafe.databinding.FragmentLocalizationBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment

class LocalizationFragment : AbstractFragment<FragmentLocalizationBinding, LocalizationViewModel>(), LocaleClickListener {

	private val adapter: LocalizationAdapter = LocalizationAdapter()

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)
		binding.localizationRecycler.layoutManager = LinearLayoutManager(layoutView.context)
		binding.localizationRecycler.adapter = adapter
		adapter.setLocaleClickListener(this)
		viewModel.requestLocales()

		binding.localizationBtn.setOnClickListener {
			if (canClick().not()) return@setOnClickListener
			if (navigatedFromRegistration)
				continueRegistration()
			else
				navigateBack()
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		viewModel.localeData.observe(viewLifecycleOwner, Observer { list ->
			adapter.setItems(list)
		})
	}

	override fun onLocaleSelected(appLocale: LocalizationViewModel.AppLocale) {
		viewModel.setNewLocale(appLocale)
	}

	private fun continueRegistration() {
		if (navigatedFromRegistration) {
			openFragmentFromRegistrationFlow(AppInfoFragment::class, Bundle().apply {
				putString(
					FirebaseCloudMessagingService.URL,
					viewModel.localizeString(URL_VIRUSAFE_WHY)
				)
			})
		}
	}

	override fun getLayoutResId(): Int = R.layout.fragment_localization

	override fun getViewModelClass(): Class<LocalizationViewModel> =
		LocalizationViewModel::class.java

	override fun getViewModelResId(): Int = BR.localizationVM

	override fun onBack(): Boolean {
		super.onBack()
		return if (navigatedFromRegistration) {
			activity?.finish()
			true
		} else {
			false
		}
	}
}
