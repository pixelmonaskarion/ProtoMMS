package io.github.pixelmonaskarion.protomms

import android.R.attr.phoneNumber
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message


data class Thread(val id: Long, val lastMessage: Message?, val address: String)
data class Contact(val id: Long, val displayName: String, val pfp_uri: String?)

var contentResolver: ContentResolver? = null;
private var context: Context? = null;

fun init(newContentResolver: ContentResolver, newContext: Context) {
    contentResolver = newContentResolver;
    context = newContext;
}

fun getInbox(): ArrayList<Message> {
    if (contentResolver == null) {
        return ArrayList()
    }
    val uri = Sms.Inbox.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, Sms.Inbox.DATE + " ASC")
    val messages = parseMessages(cursor!!)
    cursor.close()
    // Close the cursor.
    return messages
}

fun getThreadMessages(threadId: Long): ArrayList<Message> {
    val cursor = contentResolver!!.query(Sms.CONTENT_URI, null, Sms.THREAD_ID+"="+threadId, null, Sms.DEFAULT_SORT_ORDER)
    val messages = parseMessages(cursor!!)
    return messages

}

@SuppressLint("Range")
fun parseMessages(cursor: Cursor, maxMessages: Int = Int.MAX_VALUE): ArrayList<Message> {
    var messages = ArrayList<Message>()
    var currentMessage = ""
    while (cursor!!.moveToNext() && messages.size < maxMessages) {
        val body = cursor.getString(cursor.getColumnIndex(Sms.BODY))
        if (body.startsWith("\uD83D\uDC0D")) {
            currentMessage += body.substring(1)
            continue
        }
        try {
            var messageOrNull = decodeMessage(currentMessage + body)
            if (messageOrNull != null) {
                //sets the sender address to the expected value from the database to prevent stupid stuff with dubious messages
//                val finalMessage = messageOrNull.toBuilder()
//                    .setSender(messageOrNull.sender.toBuilder().setAddress(address).build()).build()
                Log.d("PCS", messageOrNull.toString())
                Log.d("PCS", "Message from SMS $currentMessage$body")
                messages.add(messageOrNull)
            }
            currentMessage = ""
        } catch (e: Exception) {
            currentMessage = ""
        }
    }
    return messages
}

@SuppressLint("Range")
fun getThreads(): ArrayList<Thread> {
    if (contentResolver == null) {
        return ArrayList()
    }
    var messages = ArrayList<Thread>()
    val uri = Sms.Conversations.CONTENT_URI
    val cursor = contentResolver!!.query(uri, null, null, null, Sms.Conversations.DEFAULT_SORT_ORDER)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndex("thread_id"))
        val snippet = cursor.getString(cursor.getColumnIndex(Sms.Conversations.SNIPPET))
        val messageCursor = contentResolver!!.query(Sms.CONTENT_URI, null, Sms.THREAD_ID+"="+id, null, Sms.DEFAULT_SORT_ORDER)
        if (messageCursor!!.moveToFirst()) {
            val address = messageCursor.getString(messageCursor.getColumnIndex(Sms.ADDRESS))
            val parsedMessages = parseMessages(messageCursor)
            messages.add(Thread(id, if (parsedMessages.size > 0) parsedMessages[0] else null, address))
        }
        messageCursor.close()
    }
    // Close the cursor.
    cursor.close()
    return messages
}

@SuppressLint("Range")
fun getContactById(id: Long): Contact? {
    if (contentResolver == null) {
        return null
    }
    val uri = Contacts.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, null)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val thisId = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
        val displayName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME))
        val pfpUri = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_URI))
        if (id == thisId) {
            return Contact(thisId, displayName, pfpUri);
        }
    }
    // Close the cursor.
    cursor.close()
    return null;
}

@SuppressLint("Range")
fun getContactByNumber(phoneNumber: String): Contact? {
    if (contentResolver == null) {
        return null
    }
    val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
    val cursor = contentResolver!!.query(uri, null, null, null, null);
    while (cursor!!.moveToNext()) {
        val displayName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME))
        val id = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
        val pfp = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_URI))
        return Contact(id, displayName, pfp)
    }
    // Close the cursor.
    cursor.close()
    return null;
}

@SuppressLint("MissingPermission", "HardwareIds")
fun getPhoneNumber(): String? {
    try {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager =
                context!!.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            manager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID)
        } else {
            val manager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            manager.line1Number
        }
    } catch (e: NullPointerException) {
        return null
    }
}

fun sendMessage(message: Message) {
    if (context == null) {
        Log.e("ProtoMMS", "No Context!")
        return;
    }
    if (contentResolver == null) {
        Log.e("ProtoMMS", "No Content Resolver!")
        return;
    }
    //WORKS BUT OPENS GUI
//    val intent = Intent(Intent.ACTION_SENDTO)
//    intent.data = Uri.parse("mms:")
//    intent.putExtra("address", address)
//    intent.putExtra("sms_body", messageText)
//    startActivity(context!!, intent, null)

    val smsManager = SmsManager.getDefault()
    message.recipientsList.forEach { recipient ->
        val messageParts = smsManager.divideMessage(encodeMessage(message));
        messageParts.forEachIndexed { i, text ->
            smsManager.sendTextMessage(recipient.address, null, if (i == messageParts.size-1) text else "\uD83D\uDC0D"+text, null, null)
        }
    }
}