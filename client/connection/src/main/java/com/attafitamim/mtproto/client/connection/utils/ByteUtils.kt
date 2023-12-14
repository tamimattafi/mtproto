package com.attafitamim.mtproto.client.connection.utils

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex() = joinToString(separator = " ") { byte ->
    byte.toHexString()
}
