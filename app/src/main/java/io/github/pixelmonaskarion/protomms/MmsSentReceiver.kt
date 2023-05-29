package io.github.pixelmonaskarion.protomms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

class MmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> Log.d("ProtoMMS", "MMS sent successfully, result code: $resultCode")
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.d("ProtoMMS", "Generic failure, result code: $resultCode")
            SmsManager.RESULT_ERROR_NO_SERVICE -> Log.d("ProtoMMS", "No service, result code: $resultCode")
            SmsManager.RESULT_ERROR_NULL_PDU -> Log.d("ProtoMMS", "Null PDU, result code: $resultCode")
            SmsManager.RESULT_ERROR_RADIO_OFF -> Log.d("ProtoMMS", "Radio off, result code: $resultCode")
            else -> Log.d("ProtoMMS", "Unknown result code: $resultCode")
        }
    }
}