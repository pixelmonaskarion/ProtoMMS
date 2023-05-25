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

fun Attachment(file: File): Attachment {
    val data = Base64.getEncoder().encodeToString(FileInputStream(file).readBytes())
    val fileName = file.name
    val mimeType = Files.probeContentType(file.toPath())
    return Attachment.newBuilder().setData(data).setFileName(fileName).setMimeType(mimeType).build()
}

@SuppressLint("Range")
fun Attachment(fileUri: Uri): Attachment {
    val data = Base64.getEncoder().encodeToString(contentResolver!!.openInputStream(fileUri)!!.readBytes())
    var fileName: String? = null;
    var cursor = contentResolver!!.query(fileUri, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
    } finally {
        cursor!!.close();
    }
    val mimeType = contentResolver!!.getType(fileUri)
    return Attachment.newBuilder().setData(data).setFileName(fileName).setMimeType(mimeType).build()
}