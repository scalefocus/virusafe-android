package bg.government.virusafe.mvvm.recycler

/**
 * An interface for all recycler items view models
 *
 * @author georgi.kushev
 */
interface IHolderViewModel {

	/**
	 * The layout resource for the view of this view model
	 * Note: it is used in the adapter as a view type
	 * and it is received in [AbstractRecyclerAdapter.onCreateViewHolder] as viewLayoutRes
	 */
	val viewLayoutRes: Int
}
