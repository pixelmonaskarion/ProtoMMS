package com.chrissytopher.pumpmessager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import android.provider.OpenableColumns
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.chrissytopher.pump.ProtoConstructors
import com.chrissytopher.pump.proto.PumpMessage.*
import com.chrissytopher.pumpmessager.receivers.MyMmsSentReceiver
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction

data class Thread(val id: Long, val lastMessage: Message?, val address: String)
data class Contact(val id: Long, val displayName: String, val pfp_uri: String?, val phoneNumber: String?)

var contentResolver: ContentResolver? = null;
private var context: Context? = null;

fun init(newContentResolver: ContentResolver, newContext: Context) {
    contentResolver = newContentResolver;
    context = newContext;
}

fun getThreadMessages(thread: Thread): ArrayList<Message> {
    val cursor = contentResolver!!.query(Mms.CONTENT_URI, null, Mms.THREAD_ID+"="+thread.id, null, Mms.DEFAULT_SORT_ORDER)
    val messages = parseMessages(cursor!!)
    cursor.close()
    messages.sortBy {
        it.sentTimestamp
    }
    return messages
}

@SuppressLint("Range")
fun getThread(threadId: Long): Thread? {
    val messageCursor = contentResolver!!.query(Telephony.Threads.CONTENT_URI, null, Mms.THREAD_ID+"="+threadId, null, Mms.DEFAULT_SORT_ORDER)
    if (messageCursor!!.moveToFirst()) {
        val address = messageCursor.getString(messageCursor.getColumnIndex(Mms.Addr.ADDRESS))
        val parsedMessages = parseMessages(messageCursor)
        if (parsedMessages.size > 0) {
            return Thread(threadId, parsedMessages[0], address)
        }
        return Thread(threadId, null, address)
    }
    return null
}

fun getLastMessage(threadId: Long): Message? {
    val messageCursor = contentResolver!!.query(Telephony.Threads.CONTENT_URI, null, Mms.THREAD_ID+"="+threadId, null, Mms.DEFAULT_SORT_ORDER)
    if (messageCursor!!.moveToFirst()) {
        val parsedMessages = parseMessages(messageCursor)
        if (parsedMessages.size > 0) {
            return parsedMessages[0]
        }
    }
    return null
}

@SuppressLint("Range")
fun parseMessages(cursor: Cursor, maxMessages: Int = Int.MAX_VALUE): ArrayList<Message> {
    var messages = ArrayList<Message>()
    var currentMessage = ""
    while (cursor!!.moveToNext() && messages.size < maxMessages) {
        val mmsId: String = cursor.getString(cursor.getColumnIndex(Mms._ID))
        val body = getMessageBody(mmsId)
        if (body != null) {
            try {
//                val messageOrNull = decodeMessage(currentMessage + body)
//                messages.add(messageOrNull)
                currentMessage = ""
            } catch (e: Exception) {
                currentMessage = ""
            }
        }
    }
    return messages
}

@SuppressLint("Range")
fun getMessageBody(mmsId: String): String? {
    val mmsPartUri = Mms.Part.CONTENT_URI
    val mmsPartCursor = contentResolver!!.query(mmsPartUri, null, "mid = ?", arrayOf(mmsId), null)

    if (mmsPartCursor != null) {
        if (mmsPartCursor.moveToFirst()) {
            do {
                val partType = mmsPartCursor.getString(mmsPartCursor.getColumnIndex(Mms.Part.CONTENT_TYPE))
                val contentId = mmsPartCursor.getString(mmsPartCursor.getColumnIndex(Mms.Part.CONTENT_ID))
                if ("text/plain" == partType && contentId == "pcs_data") {
                    val text = mmsPartCursor.getString(mmsPartCursor.getColumnIndex(Mms.Part.TEXT))
                    Log.d("ProtoMMS", "message text: $text")
                    return text
                }
            } while (mmsPartCursor.moveToNext())
        }
        mmsPartCursor.close()
    }
    return null
}

@SuppressLint("Range")
fun getThreads(): ArrayList<Thread> {
    if (contentResolver == null) {
        return ArrayList()
    }
    var messages = ArrayList<Thread>()
    val uri = Telephony.Threads.CONTENT_URI
    val cursor = contentResolver!!.query(uri, null, null, null, "date ASC")
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndex("thread_id"))
        val messageCursor = contentResolver!!.query(Mms.CONTENT_URI, null, Mms.THREAD_ID+"="+id, null, Mms.DEFAULT_SORT_ORDER)
        if (messageCursor!!.moveToFirst()) {
            val address = cursor.getString(cursor.getColumnIndex(Mms.Addr.ADDRESS))
            if (address != null) {
                val parsedMessages = parseMessages(messageCursor)
                if (parsedMessages.size > 0) {
                    messages.add(Thread(id, parsedMessages[0], address))
                }
            }
        }
        messageCursor.close()
    }
    // Close the cursor.
    cursor.close()
    messages.sortBy {
        if (it.lastMessage != null) {
            return@sortBy it.lastMessage.sentTimestamp
        } else {
            return@sortBy null
        }
    }
    messages.reverse()
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
            cursor.close()
            val phoneNumber = getPhoneNumberFromContactID(contentResolver!!, thisId.toString())
            return Contact(thisId, displayName, pfpUri, phoneNumber);
        }
    }
    // Close the cursor.
    cursor.close()
    return null;
}

@SuppressLint("Range")
fun getPhoneNumberFromContactID(contentResolver: ContentResolver, contactId: String): String? {
    var phoneNumber: String? = null

    val phoneCursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        arrayOf(contactId),
        null
    )

    phoneCursor?.use {
        if (it.moveToFirst()) {
            phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        }
    }

    return phoneNumber
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
        cursor.close()
        val phoneNumber = getPhoneNumberFromContactID(contentResolver!!, id.toString())
        return Contact(id, displayName, pfp, phoneNumber)
    }
    // Close the cursor.
    cursor.close()
    return null;
}

@SuppressLint("Range")
fun getContactByDisplayName(displayName: String): Contact? {
    if (contentResolver == null) {
        return null
    }
    val uri = Contacts.CONTENT_URI;
    val selection = "${ContactsContract.Contacts.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(displayName)

    val cursor = contentResolver!!.query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        selection,
        selectionArgs,
        null
    )
    // Iterate through the cursor and print the MMS messages.
    while (cursor!!.moveToNext()) {
        val thisId = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
        val pfpUri = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_URI))
        cursor.close()
        val phoneNumber = getPhoneNumberFromContactID(contentResolver!!, thisId.toString())
        return Contact(thisId, displayName, pfpUri, phoneNumber);
    }
    cursor.close()
    return null;
}

@SuppressLint("Range")
fun searchContacts(contactName: String): ArrayList<Contact> {
    if (contentResolver == null) {
        return arrayListOf()
    }
    var contacts = arrayListOf<Contact>()
    val searchString = "%$contactName%"

    val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_URI
    )

    val selection = "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?"
    val selectionArgs = arrayOf(searchString)

    val cursor = contentResolver!!.query(
        ContactsContract.Contacts.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )
    while (cursor!!.moveToNext()) {
        val displayName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME))
        val id = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
        val pfp = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_URI))
        val phoneNumber = getPhoneNumberFromContactID(contentResolver!!, id.toString())
        contacts.add(Contact(id, displayName, pfp, phoneNumber))
    }
    cursor.close()
    return contacts
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

@SuppressLint("Range")
fun getFileName(uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor = contentResolver!!.query(uri, null, null, null, null)!!
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun readUriToBytes(uri: Uri): ByteArray {
    val input = contentResolver!!.openInputStream(uri)
    val bytes = input!!.readBytes()
    input.close()
    return bytes
}

fun getSendMessageSettings(): Settings {
    val settings = Settings()
    settings.useSystemSending = true
    settings.deliveryReports = true
    settings.sendLongAsMms = true
    settings.sendLongAsMmsAfter = 1
    settings.group = true
    return settings
}

@SuppressLint("MissingPermission")
fun sendMessageCompat(text: String, addresses: List<String>, subId: Int?, attachments: List<Attachment>) {
    val settings = getSendMessageSettings()
    if (subId != null) {
        settings.subscriptionId = subId
    }
    if (ProtoConstructors.selfAddress == null) {
        ProtoConstructors.selfAddress = getPhoneNumber()
    }
    val message = ProtoConstructors.Message(text, addresses.toTypedArray(), attachments.toTypedArray());
    val encryptedMessage = ProtoConstructors.EncryptedMessage(message)
    val packet = ProtoConstructors.MessagePacket(encryptedMessage)
    val packetAttachment = ProtoConstructors.Attachment(writePacketToFile(packet, context!!).toString(), "application/pumppacket", "message.pumppacket")
    sendMmsMessage(text, addresses, packetAttachment, settings)
}

fun String.isPlainTextMimeType(): Boolean {
    return lowercase() == "text/plain"
}

fun sendMmsMessage(text: String, addresses: List<String>, attachment: Attachment?, settings: Settings) {
    val transaction = Transaction(context, settings)
    val message = com.klinker.android.send_message.Message(text, addresses.toTypedArray())

    if (attachment != null) {
        try {
            val uri = Uri.parse(attachment.url)
            context!!.contentResolver.openInputStream(uri)?.use {
                val bytes = it.readBytes()
                val mimeType = if (attachment.mimeType.isPlainTextMimeType()) {
                    "application/txt"
                } else {
                    attachment.mimeType
                }
                val name = attachment.fileName
                message.addMedia(bytes, mimeType, name, name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    val mmsSentIntent = Intent(context, MyMmsSentReceiver::class.java)
    transaction.setExplicitBroadcastForSentMms(mmsSentIntent)

    try {
        transaction.sendNewMessage(message)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.insertNewSMS(
    address: String,
    subject: String,
    body: String,
    date: Long,
    read: Int,
    threadId: Long,
    type: Int,
    subscriptionId: Int
): Long {
    val uri = Telephony.Sms.CONTENT_URI
    val contentValues = ContentValues().apply {
        put(Telephony.Sms.ADDRESS, address)
        put(Telephony.Sms.SUBJECT, subject)
        put(Telephony.Sms.BODY, body)
        put(Telephony.Sms.DATE, date)
        put(Telephony.Sms.READ, read)
        put(Telephony.Sms.THREAD_ID, threadId)
        put(Telephony.Sms.TYPE, type)
        put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
    }

    return try {
        val newUri = contentResolver.insert(uri, contentValues)
        newUri?.lastPathSegment?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun Context.getThreadId(address: String): Long {
    return try {
        Telephony.Threads.getOrCreateThreadId(this, address)
    } catch (e: Exception) {
        0L
    }
}