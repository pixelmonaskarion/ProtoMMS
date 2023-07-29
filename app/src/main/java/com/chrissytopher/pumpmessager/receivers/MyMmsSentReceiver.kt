package com.chrissytopher.pumpmessager.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.klinker.android.send_message.MmsSentReceiver

/** Handles updating databases and states when a MMS message is sent. */
class MyMmsSentReceiver : MmsSentReceiver() {
    override fun onMessageStatusUpdated(context: Context?, intent: Intent?, resultCode: Int) {
        super.onMessageStatusUpdated(context, intent, resultCode)
        Log.i("Me", "message status changed result: $resultCode, intent: $intent")
    }

    override fun updateInInternalDatabase(context: Context?, intent: Intent?, resultCode: Int) {
        super.updateInInternalDatabase(context, intent, resultCode)
        Log.i("Me", "message sent result code: $resultCode")
    }
}
