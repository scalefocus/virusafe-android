package bg.government.virusafe.app.home

import android.view.View
import androidx.lifecycle.LifecycleOwner
import bg.government.virusafe.R
import bg.government.virusafe.databinding.FragmentBackgroundScansBinding
import bg.government.virusafe.mvvm.fragment.AbstractFragment
import bg.government.virusafe.mvvm.viewmodel.EmptyViewModel

class BackgroundScansFragment : AbstractFragment<FragmentBackgroundScansBinding, EmptyViewModel>() {
	companion object {
		val TAG = BackgroundScansFragment::class.java.simpleName
	}

	override fun addViewModelObservers(viewLifecycleOwner: LifecycleOwner) {
		//nothing to observe
	}

	override fun getLayoutResId() = R.layout.fragment_background_scans

	override fun getViewModelClass() = EmptyViewModel::class.java

	override fun getViewModelResId() = -1

	override fun onPrepareLayout(layoutView: View) {
		super.onPrepareLayout(layoutView)

		binding.beaconsTv.text = sharedPrefsService.readStringFromSharedPrefs("beacons")
	}

	override fun bottomOfStack() = false
}
