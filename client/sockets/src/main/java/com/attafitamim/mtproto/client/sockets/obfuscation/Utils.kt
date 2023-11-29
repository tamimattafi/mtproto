package com.attafitamim.mtproto.client.sockets.obfuscation

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex() = joinToString(separator = " ") { byte ->
    byte.toHexString()
}
