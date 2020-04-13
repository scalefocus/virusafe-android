package bg.government.virusafe.mvvm.recycler

import androidx.databinding.BaseObservable

/**
 * Abstract class for view models for  Recycler view items
 *
 * @author georgi.kushev
 */
abstract class AbstractHolderViewModel<M : Any>(
	model: M? = null
) : BaseObservable(), IHolderViewModel {

	init {
		model?.let {
			updateModel(it)
		}
	}

	lateinit var model: M
		protected set

	/**
	 * Update the model used in current view model
	 * and invoke notifyChange method of the [BaseObservable]
	 */
	fun updateModel(model: M) {
		this.model = model
		onNewModelReceived(model)
		notifyChange()
	}

	/**
	 * invoked when model is changed externally
	 */
	protected abstract fun onNewModelReceived(model: M)
}
