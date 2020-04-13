package bg.government.virusafe.app.registration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsBroadcastReceiver : BroadcastReceiver() {

	private var smsBroadcastReceiverListener: SmsBroadcastReceiverListener? = null

	override fun onReceive(context: Context?, intent: Intent?) {

		if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {

			val extras = intent.extras
			val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

			when (smsRetrieverStatus.statusCode) {
				CommonStatusCodes.SUCCESS -> {
					extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT).also {
						smsBroadcastReceiverListener?.onSuccess(it)
					}
				}

				CommonStatusCodes.TIMEOUT -> {
					smsBroadcastReceiverListener?.onFailure()
				}
			}
		}
	}

	fun setListener(smsBroadcastReceiverListener: SmsBroadcastReceiverListener) {
		this.smsBroadcastReceiverListener = smsBroadcastReceiverListener
	}

	abstract class SmsBroadcastReceiverListener {
		open fun onSuccess(intent: Intent?) {
			//do nothing
		}

		open fun onFailure() {
			//do nothing
		}
	}
}
