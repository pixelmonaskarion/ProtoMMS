package io.github.pixelmonaskarion.protomms

import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Address
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import java.util.UUID


fun Message(text: String?, recipients: Array<Address>): Message {
    var message = Message.newBuilder()
    if (text != null) {
        message.text = text
    }
    recipients.forEach {
        message.addRecipients(it)
    }
    message.sender = Address(getPhoneNumber()!!)
    val uuid = UUID.randomUUID()
    message.messageId = uuid.toString()
    return message.build()
}

fun Address(address: String): Address {
    return Address.newBuilder().setAddress(address).build()
}