package io.github.pixelmonaskarion.protomms

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.OpenableColumns
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.Base64
import java.util.UUID


fun Message(text: String?, recipients: Array<Address>, attachments: Array<Attachment>): Message {
    var message = Message.newBuilder()
    if (text != null) {
        message.text = text
    }
    recipients.forEach {
        message.addRecipients(it)
    }
    for (attachment in attachments) {
        message.addAttachments(attachment)
    }
    message.sender = Address(getPhoneNumber() ?: "hopethisisanemulator!")
    val uuid = UUID.randomUUID()
    message.messageId = uuid.toString()
    message.sentTimestamp = System.currentTimeMillis()
    return message.build()
}

fun Message(text: String?, recipients: Array<Address>, attachments: Array<Attachment>, sender: Address): Message {
    var message = Message.newBuilder()
    if (text != null) {
        message.text = text
    }
    recipients.forEach {
        message.addRecipients(it)
    }
    for (attachment in attachments) {
        message.addAttachments(attachment)
    }
    message.sender = sender
    val uuid = UUID.randomUUID()
    message.messageId = uuid.toString()
    message.sentTimestamp = System.currentTimeMillis()
    return message.build()
}

fun Address(address: String): Address {
    return Address.newBuilder().setAddress(address).build()
}

@SuppressLint("Range")
fun Attachment(fileUri: Uri): Attachment {
    val mimeType = contentResolver!!.getType(fileUri)
    val url = uploadAttachment(fileUri, mimeType!!)
    var fileName: String? = null;
    var cursor = contentResolver!!.query(fileUri, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
    } finally {
        cursor!!.close();
    }
    return Attachment.newBuilder().setUrl(url).setFileName(fileName).setMimeType(mimeType).build()
}