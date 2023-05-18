package io.github.pixelmonaskarion.protomms

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.ContactsContract.Contacts
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Sms
import android.provider.Telephony.Threads
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.google.protobuf.InvalidProtocolBufferException
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import java.lang.NullPointerException


data class Thread(val id: Int, val recipients: String)
data class Contact(val id: Long, val displayName: String, val pfp_uri: String?)

var contentResolver: ContentResolver? = null;
private var context: Context? = null;

fun init(newContentResolver: ContentResolver, newContext: Context) {
    contentResolver = newContentResolver;
    context = newContext;
}

@SuppressLint("Range")
fun getInbox(): ArrayList<Message> {
    if (contentResolver == null) {
        return ArrayList()
    }
    var messages = ArrayList<Message>()
    val uri = Sms.Inbox.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, Sms.Inbox.DATE + " ASC")
    var currentMessage = ""
    while (cursor!!.moveToNext()) {
        val address = cursor.getString(cursor.getColumnIndex(Sms.ADDRESS))
        val body = cursor.getString(cursor.getColumnIndex(Sms.BODY))
        if (body.startsWith("\uD83D\uDC0D")) {
            currentMessage += body.substring(1)
            continue
        }
        try {
            var messageOrNull = decodeMessage(currentMessage + body)
            if (messageOrNull != null) {
                //sets the sender address to the expected value from the database to prevent stupid stuff with dubious messages
                val finalMessage = messageOrNull.toBuilder()
                    .setSender(messageOrNull.sender.toBuilder().setAddress(address).build()).build()
                Log.d("PCS", finalMessage.toString())
                Log.d("PCS", "Message from SMS $currentMessage$body")
                messages.add(finalMessage)
            }
            currentMessage = ""
        } catch (e: Exception) {
            currentMessage = ""
        }
    }
    // Close the cursor.
    cursor.close()
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
        val id = cursor.getInt(cursor.getColumnIndex(Sms.Conversations._ID))
        val recipients = cursor.getString(cursor.getColumnIndex(Sms.Conversations.ADDRESS))
        messages.add(Thread(id, recipients))
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