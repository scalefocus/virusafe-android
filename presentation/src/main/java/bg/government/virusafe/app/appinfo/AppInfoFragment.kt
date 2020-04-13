package bg.government.virusafe.app.appinfo

import android.os.Build
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import bg.government.virusafe.R
import bg.government.virusafe.app.fcm.FirebaseCloudMessagingService.Companion.URL
import bg.government.virusafe.app.home.HomeFragment
import bg.government.virusafe.app.registration.RegistrationFragment
import bg.government.virusafe.app.utils.CONTINUE_LABEL
import bg.government.virusafe.app.utils.NO_INTERNET_MSG
import bg.government.virusafe.app.utils.TO_THE_HOME_LABEL
import bg.government.virusafe.databinding.FragmentAppInfoBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel
import com.upnetix.presentation.view.DEFAULT_VIEW_MODEL_ID
import com.upnetix.service.util.NetworkConnectionUtil

class AppInfoFragment : AbstractFragment<FragmentAppInfoBinding, EmptyViewModel>() {

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)
		if (NetworkConnectionUtil.hasNetworkConnection(binding.root.context).not()) {
			showErrorDialog(
				message = viewModel.localizeString(NO_INTERNET_MSG),
				onErrorDialogDismiss = { navigateBack() }
			)
			return
		}
		showProgress()
		with(binding.continueBtn) {
			text =
				if (navigatedFromRegistration)
					viewModel.localizeString(CONTINUE_LABEL)
				else
					viewModel.localizeString(TO_THE_HOME_LABEL)

			setOnClickListener {
				if (canClick().not()) return@setOnClickListener
				if (navigatedFromRegistration) {
					openFragmentFromRegistrationFlow(RegistrationFragment::class)
				} else {
					navigateToView(HomeFragment::class)
				}
			}
		}
		binding.root.postDelayed({
			initWebView()
		}, DELAY)
	}

	private fun initWebView() {
		with(binding.appInfoWebview) {
			val webSettings: WebSettings = settings
			webSettings.javaScriptEnabled = true
			setLayerType(View.LAYER_TYPE_SOFTWARE, null)
			visibility = View.VISIBLE
			webViewClient = object : WebViewClient() {
				@RequiresApi(Build.VERSION_CODES.N)
				override fun shouldOverrideUrlLoading(
					view: WebView,
					request: WebResourceRequest
				): Boolean {
					view.loadUrl(request.url.toString())
					return false
				}

				@RequiresApi(Build.VERSION_CODES.M)
				override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
					view?.loadUrl(url.toString())
					return false
				}

				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					hideProgress()
				}
			}
			loadUrl(arguments?.getString(URL))
		}
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		//nothing to observe
	}

	override fun getLayoutResId() = R.layout.fragment_app_info

	override fun getViewModelClass() = EmptyViewModel::class.java

	override fun getViewModelResId() = DEFAULT_VIEW_MODEL_ID

	override fun onBack(): Boolean {
		super.onBack()
		return if (binding.appInfoWebview.copyBackForwardList().currentIndex > 0) {
			binding.appInfoWebview.goBack()
			true
		} else {
			if (navigatedFromRegistration) {
				activity?.finish()
				true
			} else {
				false
			}
		}
	}

	companion object {
		private const val DELAY = 500L
	}
}
