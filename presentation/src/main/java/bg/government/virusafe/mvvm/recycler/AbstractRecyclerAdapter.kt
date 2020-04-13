package bg.government.virusafe.mvvm.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Base implementation of recycler view adapter, which supports view models for view holders
 *
 * @param VM The view models type
 * @param VH The view holders type
 *
 * @author georgi.kushev
 */
abstract class AbstractRecyclerAdapter<VM : IHolderViewModel, VH : BaseViewHolder<VM>> : RecyclerView.Adapter<VH>() {

	private val viewModelsList = ArrayList<VM>()

	final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
		@Suppress("UNCHECKED_CAST")
		return onCreateHolder(parent, viewType) as VH
	}

	/**
	 * Similar to original onCreateViewHolder, but allows creation of multiple view holder,
	 * without casting to generic type
	 */
	abstract fun onCreateHolder(parent: ViewGroup, @LayoutRes layoutRes: Int): BaseViewHolder<*>

	override fun onBindViewHolder(holder: VH, position: Int) {
		// set view model to the holder
		holder.viewModel = viewModelsList[position]
	}

	override fun getItemCount() = viewModelsList.size

	override fun getItemViewType(position: Int) = viewModelsList[position].viewLayoutRes

	// region data set related methods
	/**
	 * Clears view models list
	 */
	fun clearItems() {
		viewModelsList.clear()
		notifyDataSetChanged()
	}

	/**
	 * Set new view models list
	 *
	 * @param itemsList The new view models list
	 */
	open fun setItems(itemsList: List<VM>) {
		viewModelsList.clear()
		viewModelsList.addAll(itemsList)
		notifyDataSetChanged()
	}
	// endregion data set related methods

	/**
	 * Use [DataBindingUtil] to inflate [ViewDataBinding]
	 *
	 * @param B The type of the view data biding
	 */
	protected fun <B : ViewDataBinding> inflateViewBinding(parent: ViewGroup, @LayoutRes layoutRes: Int): B {
		val inflater = LayoutInflater.from(parent.context)
		return DataBindingUtil.inflate(inflater, layoutRes, parent, false)
	}
}
