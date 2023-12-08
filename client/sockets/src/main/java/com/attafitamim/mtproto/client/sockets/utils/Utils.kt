package com.attafitamim.mtproto.client.sockets.utils

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex() = joinToString(separator = " ") { byte ->
    byte.toHexString()
}
