package bg.government.virusafe.app

import android.content.Intent
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
import bg.government.virusafe.app.utils.NO_INTERNET_MSG
import bg.government.virusafe.app.utils.TO_THE_HOME_LABEL
import bg.government.virusafe.databinding.FragmentWebviewBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel
import com.upnetix.applicationservice.registration.RegistrationServiceImpl.Companion.FINISHED_REGISTRATION_KEY
import com.upnetix.presentation.view.DEFAULT_VIEW_MODEL_ID
import com.upnetix.service.sharedprefs.ISharedPrefsService
import com.upnetix.service.util.NetworkConnectionUtil
import javax.inject.Inject

class WebViewFragment : AbstractFragment<FragmentWebviewBinding, EmptyViewModel>() {

	@Inject
	lateinit var sharedPreferences: ISharedPrefsService

	private var hasFinishedRegistrationFlow = true

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
		hasFinishedRegistrationFlow =
			sharedPreferences.readStringFromSharedPrefs(FINISHED_REGISTRATION_KEY).isNotEmpty()

		with(binding.homeBtn) {
			text = viewModel.localizeString(TO_THE_HOME_LABEL)
			setOnClickListener {
				if (canClick().not()) return@setOnClickListener
				if (!hasFinishedRegistrationFlow)
					openMain()
				else
					navigateToView(HomeFragment::class)
			}
		}

		binding.root.postDelayed({
			initWebView()
		}, DELAY)
	}

	private fun initWebView() {
		with(binding.webview) {
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

	override fun onBack(): Boolean {
		super.onBack()
		return if (binding.webview.copyBackForwardList().currentIndex > 0) {
			binding.webview.goBack()
			true
		} else {
			if (!hasFinishedRegistrationFlow) {
				openMain()
				true
			} else {
				false
			}
		}
	}

	private fun openMain() {
		val intent = Intent(activity, MainActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		startActivity(intent)
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		//nothing to observe
	}

	override fun getLayoutResId() = R.layout.fragment_webview

	override fun getViewModelClass() = EmptyViewModel::class.java

	override fun getViewModelResId() = DEFAULT_VIEW_MODEL_ID

	override fun bottomOfStack(): Boolean = false

	companion object {
		private const val DELAY = 500L
	}
}
