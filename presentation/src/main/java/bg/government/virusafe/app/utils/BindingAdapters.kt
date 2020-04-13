package bg.government.virusafe.app.utils

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import bg.government.virusafe.R
import bg.government.virusafe.app.personaldata.LegitimationType
import com.google.android.material.textfield.TextInputEditText
import java.util.*

@BindingAdapter("android:inputType")
fun TextInputEditText.setInputType(isEditable: Boolean) {
	inputType = if (isEditable) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_NULL
}

@BindingAdapter("inputTypeAndFilter")
fun TextInputEditText.setInputFilter(type: LegitimationType) {
	inputType = InputType.TYPE_CLASS_NUMBER
	filters = when (type) {
		LegitimationType.PERSONAL_NUMBER -> getPersonalNumberInputFilter()
		LegitimationType.FOREIGNER_NUMBER -> getForeignerNumberInputFilter()

		LegitimationType.PASSPORT -> {
			inputType = InputType.TYPE_CLASS_TEXT
			getPassportInputFilter()
		}
	}
}

@BindingAdapter("buildTypeVisibility")
fun RadioButton.setBuildTypeVisibility(buildVariant: String) {
	visibility = when (buildVariant) {
		BUILD_TYPE_MK -> View.GONE
		else -> View.VISIBLE
	}
}

@BindingAdapter(value = ["currentLocale", "buildType", "firstLogo", "secondLogo"], requireAll = true)
fun ViewGroup.setSplashLogos(locale: Locale?, buildType: String, firstLogo: ImageView, secondLogo: ImageView) {
	when (buildType) {
		BUILD_TYPE_MK -> {
			firstLogo.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.vlada_logo_small))
			secondLogo.visibility = View.GONE
		}

		else -> {
			if (locale?.language == Locale.ENGLISH.language) {
				firstLogo.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_min_health))
				secondLogo.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_oper_center))
			} else {
				firstLogo.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_min_zdrave))
				secondLogo.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_oper_shtab))
			}
		}
	}
}
