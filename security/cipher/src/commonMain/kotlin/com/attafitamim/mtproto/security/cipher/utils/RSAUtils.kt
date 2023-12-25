package com.attafitamim.mtproto.security.cipher.utils

import com.attafitamim.mtproto.security.cipher.rsa.RsaKey

private const val HEADER = "3082010a02820101"
private const val PREFIX = "00"
private const val SHORT_PREFIX = "0"
private const val POSTFIX = "0203"

@OptIn(ExperimentalStdlibApi::class)
fun RsaKey.Raw.toDERFormat(): ByteArray {
    val hexData = when (type) {
        RsaKey.Type.PUBLIC -> toPublicDERFormat()
        RsaKey.Type.PRIVATE -> TODO("Not yet implemented")
    }

    return hexData.hexToByteArray()
}

fun RsaKey.Raw.toPublicDERFormat(): String {
    val hexBuilder = StringBuilder(HEADER)

    val additionalPrefix = when {
        modulusHex.startsWith(PREFIX) -> null
        modulusHex.startsWith(SHORT_PREFIX) -> SHORT_PREFIX
        else -> PREFIX
    }


    if (!additionalPrefix.isNullOrBlank()) {
        hexBuilder.append(additionalPrefix)
    }


    hexBuilder.append(modulusHex)
    hexBuilder.append(POSTFIX, exponentHex)

    return hexBuilder.toString()
}
