package io.github.pixelmonaskarion.xmlsms

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.ContactsContract.Contacts
import android.provider.Telephony
import android.provider.Telephony.Sms
import android.provider.Telephony.Threads
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import io.github.pixelmonaskarion.xmlsms.ui.theme.XMLSMSTheme

data class Message(val author: String, val sender_id: Int, val body: String)
data class Thread(val id: Int, val recipients: String)
data class Contact(val id: Int, val displayName: String, val pfp_uri: String?)

private var contentResolver: ContentResolver? = null;

fun init(newContentResolver: ContentResolver) {
    contentResolver = newContentResolver;
}

@SuppressLint("Range")
fun getInbox(): ArrayList<Message> {
    if (contentResolver == null) {
        return ArrayList()
    }
    var messages = ArrayList<Message>()
    val uri = Telephony.Sms.Inbox.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, null)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val id = cursor.getInt(cursor.getColumnIndex(Sms._ID))
        val address = cursor.getString(cursor.getColumnIndex(Sms.ADDRESS))
        val senderId = cursor.getInt(cursor.getColumnIndex(Sms.PERSON))
        val type = cursor.getInt(cursor.getColumnIndex(Sms.TYPE))
        val date = cursor.getString(cursor.getColumnIndex(Sms.DATE))
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
fun getContactById(id: Int): Contact? {
    if (contentResolver == null) {
        return null
    }
    val uri = Contacts.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, null)
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val thisId = cursor.getInt(cursor.getColumnIndex(Contacts._ID))
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
    var smsManager = SmsManager.getDefault()
    smsManager.sendTextMessage(address, null, messageText, null, null);
}