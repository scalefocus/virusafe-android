package bg.government.virusafe.mvvm.recycler

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Base implementation of [RecyclerView.ViewHolder] used in [AbstractRecyclerAdapter]
 *
 * @param binding The view data binding used in the view holder
 * @param vmBindingRes The binding resource for the view model
 * @param VM The view model type
 *
 * @author georgi.kushev
 */
open class BaseViewHolder<VM : IHolderViewModel>(
	protected val binding: ViewDataBinding,
	private val vmBindingRes: Int
) : RecyclerView.ViewHolder(binding.root) {

	var viewModel: VM? = null
		set(value) {
			field = value
			binding.setVariable(vmBindingRes, value)
			binding.executePendingBindings()
		}

	/**
	 * Set row click listener to the binding of the view in current holder
	 *
	 * @param listenerBindingRes The binding resource for the row click listener
	 * @param rowClickListener The click listener for the binding of the view in the holder
	 */
	fun setRowClickListener(listenerBindingRes: Int, rowClickListener: RowClickListener) {
		binding.setVariable(listenerBindingRes, rowClickListener)
	}

	/**
	 * A marker interface for all click handlers in the view holders
	 */
	interface RowClickListener
}
