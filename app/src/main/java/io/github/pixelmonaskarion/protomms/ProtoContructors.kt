package io.github.pixelmonaskarion.protomms

import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Recipient
import java.util.UUID


fun Message(text: String?, recipients: Array<Recipient>): Message {
    var message = Message.newBuilder()
    if (text != null) {
        message.text = text
    }
    recipients.forEach {
        message.addRecipients(it)
    }
    val uuid = UUID.randomUUID()
    message.messageId = uuid.toString()
    return message.build()
}

fun Recipient(address: String): Recipient {
    return Recipient.newBuilder().setAddress(address).build()
}