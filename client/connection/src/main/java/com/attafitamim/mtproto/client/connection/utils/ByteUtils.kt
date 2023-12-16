package com.attafitamim.mtproto.client.connection.utils

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex(appendSpaces: Boolean = true): String {
    val separator = if (appendSpaces) {
        " "
    } else {
        ""
    }

    return joinToString(separator = separator) { byte ->
        byte.toHexString()
    }
}
