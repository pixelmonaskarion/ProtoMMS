package com.chrissytopher.pumpmessager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.chrissytopher.pumpmessager.getThreadId
import com.chrissytopher.pumpmessager.insertNewSMS

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        var address = ""
        var subject = ""
        var body = ""
        var date = 0L
        var threadId = 0L
        val type = Telephony.Sms.MESSAGE_TYPE_INBOX
        val subscriptionId = intent.getIntExtra("subscription", -1)
        for (message in messages) {
            address = message.originatingAddress ?: ""
            subject = message.pseudoSubject
            body += message.messageBody
            date = System.currentTimeMillis()
            threadId = context.getThreadId(address)
        }
        Log.i("SmsReceiver", "Received message \"$body\" from $address")
        context.insertNewSMS(address, subject, body, date, 0, threadId, type, subscriptionId)
    }
}