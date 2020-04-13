package bg.government.virusafe.app.selfcheck

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

private const val DIALOG_TITLE = "dialog_title"
private const val DIALOG_DESCRIPTION = "dialog_description"
private const val DIALOG_PRIMARY_BTN = "dialog_primary_btn"

class FillSymptomsDialog : DialogFragment() {

	private var primaryButtonListener: OnDialogButtonListener? = null

	companion object {
		internal fun newInstance(
			title: String? = null,
			description: String,
			primaryButtonText: String
		) = FillSymptomsDialog(
		).apply {
			arguments = Bundle().apply {
				putString(DIALOG_TITLE, title)
				putString(DIALOG_DESCRIPTION, description)
				putString(DIALOG_PRIMARY_BTN, primaryButtonText)
			}
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val title = arguments?.getString(DIALOG_TITLE)
		val description = arguments?.getString(DIALOG_DESCRIPTION)
		val primaryButtonText = arguments?.getString(DIALOG_PRIMARY_BTN)

		return activity?.let {
			// Use the Builder class for convenient dialog construction
			val builder = AlertDialog.Builder(it)
			builder.setTitle(title)
			builder.setMessage(description)

			primaryButtonText?.let { primaryButtonText ->
				builder.setPositiveButton(primaryButtonText) { _, _ -> primaryButtonListener?.onClicked() }
			}

			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}
}

interface OnDialogButtonListener {
	fun onClicked()
}
