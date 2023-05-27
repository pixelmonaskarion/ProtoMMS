package io.github.pixelmonaskarion.protomms

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import android.provider.OpenableColumns
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
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
    cursor.close()
    messages.sortBy {
        it.sentTimestamp
    }
    return messages
}

@SuppressLint("Range")
fun getThread(threadId: Long): Thread? {
    val messageCursor = contentResolver!!.query(Sms.CONTENT_URI, null, Sms.THREAD_ID+"="+threadId, null, Sms.DEFAULT_SORT_ORDER)
    if (messageCursor!!.moveToFirst()) {
        val address = messageCursor.getString(messageCursor.getColumnIndex(Sms.ADDRESS))
        val parsedMessages = parseMessages(messageCursor)
        if (parsedMessages.size > 0) {
            return Thread(threadId, parsedMessages[0], address)
        }
        return Thread(threadId, null, address)
    }
    return null
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
            val messageOrNull = decodeMessage(currentMessage + body)
            messages.add(messageOrNull)
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
            if (parsedMessages.size > 0) {
                messages.add(Thread(id, parsedMessages[0], address))
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
    val URL = "http://ec2-34-220-175-228.us-west-2.compute.amazonaws.com";
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