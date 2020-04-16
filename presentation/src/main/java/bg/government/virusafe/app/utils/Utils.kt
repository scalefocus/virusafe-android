package bg.government.virusafe.app.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import com.upnetix.applicationservice.geolocation.LocationEntity

/**
 * Helper function to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

	// Background permissions didn't exit prior to Q, so it's approved by default.
	if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
		android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
	) {

		return true
	}

	return ActivityCompat.checkSelfPermission(this, permission) ==
			PackageManager.PERMISSION_GRANTED
}

/**
 * Helper function to check if the app is on the foreground.
 */
fun Context.isAppInForeground(): Boolean {
	val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
	val appProcesses = activityManager.runningAppProcesses ?: return false

	val packageName = this.packageName

	for (appProcess in appProcesses) {
		if (appProcess.importance ==
			ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
			appProcess.processName == packageName
		) {
			return true
		}
	}
	return false
}

fun Location.toLocationEntity(): LocationEntity =
	LocationEntity(
		this.latitude,
		this.longitude,
		this.time
	)

fun <T> SparseArray<T>.values(): List<T> {
	val list = ArrayList<T>()
	forEach { _, value ->
		list.add(value)
	}
	return list.toList()
}

fun TextView.setClickablePhrase(
	fullText: String?,
	clickablePhrase: String?,
	shouldBoldPhrase: Boolean?,
	shouldUnderlinePhrase: Boolean = true,
	@ColorInt phraseColor: Int? = null,
	onClick: (() -> Unit)? = null
) {
	if (fullText == null) {
		return
	}
	if (clickablePhrase == null || shouldBoldPhrase == null || onClick == null) {
		return
	}

	val formattedFullText = String.format(fullText, clickablePhrase)
	val spannableString = SpannableString(formattedFullText)
	val phraseIndex = formattedFullText.indexOf(clickablePhrase, 0)
	if (phraseIndex != -1) {
		// Make the clickable phrase bold if shouldBoldPhrase is set to true
		if (shouldBoldPhrase) {
			val boldSpan = StyleSpan(Typeface.BOLD)
			spannableString.setSpan(
				boldSpan, phraseIndex,
				phraseIndex + clickablePhrase.length,
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE
			)
		}

		// Creates a clickable span, draws an underline and handles the clicks
		movementMethod = LinkMovementMethod.getInstance()
		val clickableSpan = object : ClickableSpan() {
			override fun onClick(widget: View) {
				onClick.invoke()
			}

			override fun updateDrawState(drawState: TextPaint) {
				super.updateDrawState(drawState)
				drawState.isUnderlineText = shouldUnderlinePhrase
				phraseColor?.let { drawState.color = it }
			}
		}

		spannableString.setSpan(
			clickableSpan, phraseIndex,
			phraseIndex + clickablePhrase.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
		)
	}
	text = spannableString
}
