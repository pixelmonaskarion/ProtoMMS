package io.github.pixelmonaskarion.protomms

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentResolver
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
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.FileProvider
import com.google.mms.pdu.EncodedStringValue
import com.google.mms.pdu.PduBody
import com.google.mms.pdu.PduComposer
import com.google.mms.pdu.PduPart
import com.google.mms.pdu.SendReq
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.Random
import java.util.UUID


data class Thread(val id: Long, val lastMessage: Message?, val address: String)
data class Contact(val id: Long, val displayName: String, val pfp_uri: String?, val phoneNumber: String?)

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
    val uri = Mms.Inbox.CONTENT_URI;
    val cursor = contentResolver!!.query(uri, null, null, null, Mms.Inbox.DATE + " ASC")
    val messages = parseMessages(cursor!!)
    cursor.close()
    // Close the cursor.
    return messages
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
                val messageOrNull = decodeMessage(currentMessage + body)
                messages.add(messageOrNull)
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

suspend fun sendMessage(message: Message) {
    coroutineScope {
        async {
            if (context == null) {
                Log.e("ProtoMMS", "No Context!")
                return@async;
            }
            if (contentResolver == null) {
                Log.e("ProtoMMS", "No Content Resolver!")
                return@async;
            }
            val smsManager = SmsManager.getDefault()
            val encodedMessage = encodeMessage(message)
            message.recipientsList.forEach { recipient ->
//                val sendRequestPdu = SendReq()
//                Log.d("ProtoMMS", recipient.address)
//                sendRequestPdu.addTo(EncodedStringValue(recipient.address))
//
//                val pduBody = PduBody()
//
////                val textPduPart = PduPart()
////                textPduPart.data = encodedMessage.encodeToByteArray()
////                textPduPart.contentType = "text/plain".encodeToByteArray()
////                textPduPart.filename = "bodytext.txt".encodeToByteArray()
//////                textPduPart.contentId = "pcs_data".encodeToByteArray()
////                pduBody.addPart(textPduPart)
//
//                val apologyPduPart = PduPart()
//                apologyPduPart.data = "sorry if you get this message, my code is going very wrong".encodeToByteArray()
//                apologyPduPart.contentType = "text/plain".encodeToByteArray()
//                apologyPduPart.filename = "apologytext".encodeToByteArray()
//                pduBody.addPart(apologyPduPart)
//
//                sendRequestPdu.body = pduBody
//
//                val composer = PduComposer(sendRequestPdu)
//                val pduData = composer.make()
//                val pduFile = File.createTempFile("temp_mms"+message.messageId, null, context!!.cacheDir)
//                pduFile.outputStream().write(pduData)
//                val pduUri = FileProvider.getUriForFile(context!!, context!!.packageName + ".fileprovider", pduFile)
//
//                val sentIntent = Intent(Intent.ACTION_SEND)
//                val sentPI = PendingIntent.getBroadcast(
//                    context,
//                    0,
//                    sentIntent,
//                    PendingIntent.FLAG_MUTABLE
//                )
//
//                smsManager.sendMultimediaMessage(context, pduUri, null,null, null)
                val sendReq = SendReq()
                sendReq.addTo(EncodedStringValue(recipient.address));
                val pduBody = PduBody()
                val bodyPart = PduPart()
                bodyPart.data = encodedMessage.encodeToByteArray()
                bodyPart.contentType = "text/plain".encodeToByteArray()
                bodyPart.filename = "bodytext2".encodeToByteArray()
                bodyPart.contentId = Random().nextInt().toString().encodeToByteArray()
                pduBody.addPart(bodyPart)
                sendReq.transactionId = Random().nextInt().toString().encodeToByteArray()
                sendReq.body = pduBody
                val composer = PduComposer(sendReq)
                val pduData = composer.make()
                val pduFile = File.createTempFile("temp_mms", null, context!!.cacheDir)
                pduFile.outputStream().write(pduData)
                val pduUri = FileProvider.getUriForFile(context!!, context!!.packageName + ".fileprovider", pduFile)
                Log.d("ProtoMMS", "sending MMS")
                val sentIntent = Intent("com.example.ACTION_MMS_SENT")
                val sentPI = PendingIntent.getBroadcast(
                    context,
                    0,
                    sentIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                smsManager.sendMultimediaMessage(context!!, pduUri, null, null, sentPI)
            }
        }
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

fun uploadAttachment(uri: Uri, mimeType: String): String {
    val URL = "https://pcs.chrissytopher.com";
    val uuid = UUID.randomUUID().toString()
    val extension = contentResolver!!.getType(uri)!!
    val client = OkHttpClient()
    val uriByteArray = readUriToBytes(uri)
    val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart(
            "file", getFileName(uri),
            uriByteArray.toRequestBody(mimeType.toMediaTypeOrNull(), 0, uriByteArray.size)
        )
        .addFormDataPart("uuid", uuid)
        .addFormDataPart("extension", extension)
        .build()
    val request = Request.Builder()
            //no one use my ec2 for web storage lol
        .url("$URL/post-attachment")
        .post(requestBody)
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                // Handle the error
            } else {
                // Handle the response
                val responseData = response.body!!.string()
                // Do something with the response
            }
        }
    })
    return "$URL/$uuid.$extension"
}