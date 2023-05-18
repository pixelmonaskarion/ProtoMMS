package io.github.pixelmonaskarion.protomms

import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import java.io.ByteArrayOutputStream
import java.util.Base64

fun encodeMessage(message: Message): String {
    var baos = ByteArrayOutputStream()
    message.writeTo(baos)
    return Base64.getEncoder().encodeToString(baos.toByteArray())
}

fun decodeMessage(protoString: String): Message {
    return Message.parseFrom(Base64.getDecoder().decode(protoString.toByteArray()))
}