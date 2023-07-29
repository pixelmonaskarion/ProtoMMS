package com.chrissytopher.pumpmessager

import android.content.Context
import android.net.Uri
import com.chrissytopher.pump.Serialization
import com.chrissytopher.pump.proto.PumpMessage.Packet
import java.io.File

fun writePacketToFile(packet: Packet, context: Context): Uri {
    val bytes = Serialization.serializePacket(packet)
    val file = File.createTempFile("packet", ".pumppacket", context.cacheDir)
    file.writeBytes(bytes)
    return Uri.fromFile(file)
}
