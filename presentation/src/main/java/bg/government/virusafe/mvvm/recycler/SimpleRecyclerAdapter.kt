package bg.government.virusafe.mvvm.recycler

import android.view.ViewGroup

/**
 * A simple implementation of the [AbstractRecyclerAdapter],
 * which can be used in cases when custom implementation of the [BaseViewHolder] in not required
 *
 * @param vmBindingRes The binding resource for the view model (example BR.rowViewModel)
 *
 * @author georgi.kushev
 */
class SimpleRecyclerAdapter(
	private val vmBindingRes: Int
) : AbstractRecyclerAdapter<IHolderViewModel, BaseViewHolder<IHolderViewModel>>() {

	private var listenerBindingRes: Int = 0
	private var rowClickListener: BaseViewHolder.RowClickListener? = null

	/**
	 * Set row click listener to the binding of the view in current holder
	 *
	 * @param listenerBindingRes The binding resource for the row click listener
	 * @param rowClickListener The click listener for the binding of the view in the holder
	 */
	fun setRowClickListener(listenerBindingRes: Int, rowClickListener: BaseViewHolder.RowClickListener) {
		this.listenerBindingRes = listenerBindingRes
		this.rowClickListener = rowClickListener
	}

	override fun onCreateHolder(parent: ViewGroup, layoutRes: Int): BaseViewHolder<*> {
		val holder = BaseViewHolder<IHolderViewModel>(inflateViewBinding(parent, layoutRes), vmBindingRes)
		rowClickListener?.let {
			holder.setRowClickListener(listenerBindingRes, it)
		}
		return holder
	}
}
