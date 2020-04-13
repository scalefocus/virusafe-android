package bg.government.virusafe.app.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.WebViewFragment
import bg.government.virusafe.app.appinfo.AppInfoFragment
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService.Companion.URL
import bg.government.virusafe.app.localization.LocalizationFragment
import bg.government.virusafe.app.personaldata.PersonalDataFragment
import bg.government.virusafe.app.selfcheck.SelfCheckFragment
import bg.government.virusafe.app.splash.SplashActivity.Companion.STATISTICS_URL_KEY
import bg.government.virusafe.app.utils.URL_ABOUT_COVID
import bg.government.virusafe.app.utils.URL_VIRUSAFE_WHY
import bg.government.virusafe.databinding.FragmentHomeBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment

class HomeFragment : AbstractFragment<FragmentHomeBinding, HomeViewModel>() {

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)

		setStatusBarColor()

		binding.fragmentHomeBtnSelfCheck.setOnClickListener {
			if (canClick().not()) return@setOnClickListener

			navigateToView(SelfCheckFragment::class)
		}

		binding.fragmentHomeBtnStatistics.setOnClickListener {
			if (canClick().not()) return@setOnClickListener

			navigateToView(WebViewFragment::class, Bundle().apply {
				putString(URL, sharedPrefsService.readStringFromSharedPrefs(STATISTICS_URL_KEY))
			})
		}

		binding.fragmentHomePersonalInfo.setOnClickListener {
			if (canClick().not()) return@setOnClickListener

			navigateToView(PersonalDataFragment::class)
		}

		binding.fragmentHomeBtnAppInfo.setOnClickListener {
			if (canClick().not()) return@setOnClickListener

			navigateToView(AppInfoFragment::class, Bundle().apply {
				putString(URL, viewModel.localizeString(URL_VIRUSAFE_WHY))
			})
		}

		binding.homeScreenLearnMoreBtn.setOnClickListener {
			if (canClick().not()) return@setOnClickListener

			navigateToView(WebViewFragment::class, Bundle().apply {
				putString(URL, viewModel.localizeString(URL_ABOUT_COVID))
			})
		}

		binding.homeScreenTermsBtn.setOnClickListener {
			if (canClick()) {
				val fm: FragmentManager =
					activity?.supportFragmentManager ?: return@setOnClickListener
				TermsAndConditionsDialog().show(
					fm,
					TermsAndConditionsDialog::class.java.canonicalName
				)
			}
		}

		binding.languageContainer.setOnClickListener {
			if (canClick().not()) return@setOnClickListener
			navigateToView(LocalizationFragment::class)
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		//nothing to observe
	}

	override fun getLayoutResId() = R.layout.fragment_home

	override fun getViewModelClass() = HomeViewModel::class.java

	override fun getViewModelResId() = BR.homeVM

	override fun onBack(): Boolean {
		super.onBack()
		activity?.finish()
		return true
	}

	override fun bottomOfStack(): Boolean {
		return true
	}

	private fun setStatusBarColor() {
		val window: Window? = activity?.window
		window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
		window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		window?.statusBarColor = ContextCompat.getColor(activity!!, R.color.color_light_blue)
	}
}
