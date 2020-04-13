package bg.government.virusafe.app.selfcheck

import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.home.HomeFragment
import bg.government.virusafe.app.splash.SplashActivity
import bg.government.virusafe.databinding.FragmentDoneBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel

class DoneFragment : AbstractFragment<FragmentDoneBinding, EmptyViewModel>() {

	private var isAnimFinished = false

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		//do nothing
	}

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)
		binding.doneBackToHomeBtn.setOnClickListener {
			if (canClick()) navigateToHome()
		}
		binding.facebookShareBtn.setOnClickListener {
			sharingToSocialMedia(FACEBOOK_PACKAGE)
		}
		binding.instagramShareBtn.setOnClickListener {
			sharingToSocialMedia(INSTAGRAM_PACKAGE)
		}
		binding.twiterShareBtn.setOnClickListener {
			sharingToSocialMedia(TWITTER_PACKAGE)
		}
		binding.linkedinShareBtn.setOnClickListener {
			sharingToSocialMedia(LINKEDIN_PACKAGE)
		}
	}

	override fun onResume() {
		super.onResume()
		if (isAnimFinished)
			return
		binding.doneImg.postDelayed({
			binding.doneImg.check()
			isAnimFinished = true
		}, DELAY)
	}

	override fun getLayoutResId(): Int = R.layout.fragment_done

	override fun getViewModelClass(): Class<EmptyViewModel> = EmptyViewModel::class.java

	override fun getViewModelResId(): Int = BR.doneVM

	override fun onBack(): Boolean {
		super.onBack()
		if (navigatedFromRegistration) {
			activity?.finish()
		} else {
			navigateToHome()
		}
		return true
	}

	private fun navigateToHome() {
		if (navigatedFromRegistration) {
			val intent = Intent(activity, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			startActivity(intent)
		} else {
			navigateToView(HomeFragment::class)
		}
	}

	private fun sharingToSocialMedia(application: String) {
		if (!checkAppInstall(application)) {
			Toast.makeText(
				activity?.applicationContext,
				getString(R.string.application_is_not_installed), Toast.LENGTH_LONG
			).show()
			return
		}

		val appGooglePlayStoreUrl = SplashActivity.getAppGooglePlayStoreUrl(sharedPrefsService)

		val intent = Intent()
		intent.action = Intent.ACTION_SEND
		intent.type = TEXT_PLAIN

		if (application == FACEBOOK_PACKAGE)
			intent.putExtra(Intent.EXTRA_TEXT, appGooglePlayStoreUrl)
		else
			intent.putExtra(
				Intent.EXTRA_TEXT, TextUtils.concat(
					getString(R.string.share_app_text), "\n",
					appGooglePlayStoreUrl
				)
			)

		intent.setPackage(application)
		startActivity(intent)
	}

	private fun checkAppInstall(uri: String): Boolean {
		val pm: PackageManager? = activity?.packageManager
		try {
			pm?.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
			return true
		} catch (e: PackageManager.NameNotFoundException) {
		}
		return false
	}

	companion object {
		private const val DELAY = 300L

		private const val FACEBOOK_PACKAGE = "com.facebook.katana"
		private const val INSTAGRAM_PACKAGE = "com.instagram.android"
		private const val TWITTER_PACKAGE = "com.twitter.android"
		private const val LINKEDIN_PACKAGE = "com.linkedin.android"
		private const val TEXT_PLAIN = "text/plain"
	}
}
