package com.chrissytopher.pumpmessager.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.klinker.android.send_message.SentReceiver


class SmsSentReceiverProxy : SentReceiver() {
    /*
     * (non-Javadoc)
     * @see com.klinker.android.send_message.StatusUpdatedReceiver#onMessageStatusUpdated(android.content.Context, android.content.Intent, int)
     */
    override fun onMessageStatusUpdated(context: Context, intent: Intent, receiverResultCode: Int) {
        // TODO Auto-generated method stub
        Log.v(
            DEBUG_TAG,
            "SMS Receiver Result Code = [$receiverResultCode]"
        )
    }

    companion object {
        private val DEBUG_TAG = SmsSentReceiverProxy::class.java.simpleName
    }
}