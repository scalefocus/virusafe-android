package bg.government.virusafe.mvvm.activity

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentTransaction
import bg.government.virusafe.R
import bg.government.virusafe.app.utils.ClickHandler
import bg.government.virusafe.mvvm.fragment.AbstractFragment.Companion.NAVIGATED_FROM_REGISTRATION
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.presentation.view.BaseActivity
import com.upnetix.presentation.view.IView
import kotlin.reflect.KClass

/**
 * Abstract class for creating activities.
 *
 * @param B  the activity binding type
 * @param VM the view model
 * @author stoyan.yanev
 */
abstract class AbstractActivity<B, VM> : BaseActivity<B, VM>()
		where B : ViewDataBinding, VM : AbstractViewModel {

	private var clickHandler: ClickHandler? = null

	override fun setTransactionCustomAnimation(fragmentTransaction: FragmentTransaction) {
		currentView?.let {
			fragmentTransaction.setCustomAnimations(
				R.anim.enter_from_right,
				R.anim.exit_to_left,
				R.anim.enter_from_left,
				R.anim.exit_to_right
			)
		}
	}

	open fun getProgressIndicator(): ProgressBar? {
		return null
	}

	fun showProgress() {
		getProgressIndicator()?.visibility = View.VISIBLE
	}

	fun hideProgress() {
		getProgressIndicator()?.visibility = View.GONE
	}

	fun canClick(): Boolean {
		if (clickHandler == null) {
			clickHandler = ClickHandler()
		}
		return clickHandler?.canPerformClick() ?: true
	}

	protected fun <T : IView<*>> openFragmentFromRegistrationFlow(
		clazz: KClass<T>, bundle: Bundle? = null
	) {
		val newBundle = Bundle().apply {
			putBoolean(NAVIGATED_FROM_REGISTRATION, true)
			bundle?.let {
				putAll(it)
			}
		}
		openView(clazz, newBundle)
	}
}

