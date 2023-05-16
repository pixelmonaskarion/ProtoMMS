package io.github.pixelmonaskarion.protomms

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract.Contacts
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Sms
import android.provider.Telephony.Threads
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import io.github.pixelmonaskarion.protomms.mms.pdu.PduHeaders


data class Message(val author: String, val sender_id: Long, val body: String)
data class Thread(val id: Int, val recipients: String)
data class Contact(val id: Long, val displayName: String, val pfp_uri: String?)

private var contentResolver: ContentResolver? = null;
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
    val cursor = contentResolver!!.query(uri, null, null, null, null)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val id = cursor.getInt(cursor.getColumnIndex(Mms._ID))
        val addrUri = Mms.Addr.getAddrUriForMessage(id.toString());
        val addrCursor = contentResolver!!.query(addrUri, null, null, null, null)
        var address = "";
        var senderId = 0L;
        while (addrCursor!!.moveToNext()) {
            val type = addrCursor.getInt(addrCursor.getColumnIndex(Mms.Addr.TYPE))
            if (type == PduHeaders.FROM) {
                address = addrCursor.getString(addrCursor.getColumnIndex(Mms.Addr.ADDRESS))
                senderId = addrCursor.getLong(addrCursor.getColumnIndex(Mms.Addr.CONTACT_ID))
            }
        }
        val body = cursor.getString(cursor.getColumnIndex(Sms.BODY))
        messages.add(Message(address, senderId, body))
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
    val uri = Telephony.Threads.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, null)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val id = cursor.getInt(cursor.getColumnIndex(Threads._ID))
        val recipients = cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS))
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

fun sendSMS(messageText: String, address: String) {
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
    smsManager.divideMessage(messageText).forEach {
        smsManager.sendTextMessage(address, null, it, null, null)
    }
}