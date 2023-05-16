package io.github.pixelmonaskarion.protomms

import io.github.pixelmonaskarion.protomms.proto.ProtoMms
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

fun encodeMessage(messageText: String): String {
    var messageBuilder = ProtoMms.Message.newBuilder()
    messageBuilder.text = messageText
    var baos = ByteArrayOutputStream()
    messageBuilder.build().writeTo(baos)
    val protoString = String(baos.toByteArray(), Charset.defaultCharset());
    return protoString
}

fun decodeMessage(protoString: String): String {
    val messageProto = ProtoMms.Message.parseFrom(protoString.encodeToByteArray())
    return messageProto.text
}

