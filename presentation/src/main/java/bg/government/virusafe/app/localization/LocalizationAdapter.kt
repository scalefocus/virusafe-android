package bg.government.virusafe.app.localization

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.databinding.RowAppLocaleBinding

class LocalizationAdapter : RecyclerView.Adapter<LocalizationViewHolder>() {

	private val modelList = ArrayList<LocalizationViewModel.AppLocale>()
	private var questionClickListener: LocaleClickListener? = null

	fun setItems(itemsList: List<LocalizationViewModel.AppLocale>) {
		modelList.clear()
		modelList.addAll(itemsList)
		notifyDataSetChanged()
	}

	fun setLocaleClickListener(listener: LocaleClickListener) {
		questionClickListener = listener
	}

	override fun getItemCount(): Int = modelList.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalizationViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding =
			DataBindingUtil.inflate<RowAppLocaleBinding>(
				inflater,
				R.layout.row_app_locale,
				parent,
				false
			)
		return LocalizationViewHolder(binding, questionClickListener)
	}

	override fun onBindViewHolder(holder: LocalizationViewHolder, position: Int) {
		holder.bind(modelList[position])
	}
}

class LocalizationViewHolder(
	private val binding: RowAppLocaleBinding,
	private val listener: LocaleClickListener?
) : RecyclerView.ViewHolder(binding.root) {

	var model: LocalizationViewModel.AppLocale? = null
		private set

	init {
		binding.localeContainer.setOnClickListener {
			model?.let {
				listener?.onLocaleSelected(it)
			}
		}
	}

	fun bind(model: LocalizationViewModel.AppLocale) {
		this.model = model
		with(binding) {
			setVariable(BR.modelAppLocale, model)
			executePendingBindings()
		}
	}
}

interface LocaleClickListener {

	fun onLocaleSelected(appLocale: LocalizationViewModel.AppLocale)
}
