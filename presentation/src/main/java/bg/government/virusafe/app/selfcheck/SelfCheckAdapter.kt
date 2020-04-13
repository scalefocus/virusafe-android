package bg.government.virusafe.app.selfcheck

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import bg.government.virusafe.BR
import bg.government.virusafe.R
import bg.government.virusafe.databinding.RowQuestionSelfCheckBinding
import com.upnetix.applicationservice.selfcheck.model.Question

class SelfCheckAdapter() :
	RecyclerView.Adapter<SelfCheckViewHolder>() {

	private val modelList = ArrayList<Question>()
	private var questionClickListener: QuestionClickListener? = null
	private var yesString: String? = null
	private var noString: String? = null

	fun setItems(itemsList: List<Question>) {
		modelList.clear()
		modelList.addAll(itemsList)
		notifyDataSetChanged()
	}

	fun setQuestionClickListener(listener: QuestionClickListener) {
		questionClickListener = listener
	}

	fun setTranslations(yesString: String, noString: String) {
		this.yesString = yesString
		this.noString = noString
	}

	override fun getItemCount(): Int = modelList.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelfCheckViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val binding =
			DataBindingUtil.inflate<RowQuestionSelfCheckBinding>(
				inflater,
				R.layout.row_question_self_check,
				parent,
				false
			)
		return SelfCheckViewHolder(binding, questionClickListener)
	}

	override fun onBindViewHolder(holder: SelfCheckViewHolder, position: Int) {
		holder.bind(modelList[position], yesString.orEmpty(), noString.orEmpty())
	}
}

class SelfCheckViewHolder(
	private val binding: RowQuestionSelfCheckBinding,
	private val listener: QuestionClickListener?
) : RecyclerView.ViewHolder(binding.root) {

	var model: Question? = null
		private set

	init {
		binding.questionAnswerYesBtn.setOnClickListener {
			model?.let {
				it.answer = true
				binding.questionAnswerNoBtn.isChecked = false
				listener?.onQuestionAnswered(it)
			}
		}
		binding.questionAnswerNoBtn.setOnClickListener {
			model?.let {
				it.answer = false
				binding.questionAnswerYesBtn.isChecked = false
				listener?.onQuestionAnswered(it)
			}
		}
	}

	fun bind(model: Question, yesString: String, noString: String) {
		this.model = model
		with(binding) {
			questionAnswerNoBtn.text = noString
			questionAnswerYesBtn.text = yesString
			setVariable(BR.modelQuestion, model)
			executePendingBindings()
		}
	}
}

interface QuestionClickListener {

	fun onQuestionAnswered(question: Question)
}
