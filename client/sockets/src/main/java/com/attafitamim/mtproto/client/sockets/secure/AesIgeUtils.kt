package com.attafitamim.mtproto.client.sockets.secure

import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.ige.AesIgeCipher


fun decryptAesIge(
    key: ByteArray,
    iv: ByteArray,
    data: ByteArray
): ByteArray {
    val cipher = AesIgeCipher(CipherMode.DECRYPT).apply {
        init(key, iv)
    }

    return cipher.finalize(data)
}
fun encryptAesIge(
    key: ByteArray,
    iv: ByteArray,
    data: ByteArray
): ByteArray {
    val cipher = AesIgeCipher(CipherMode.ENCRYPT).apply {
        init(key, iv)
    }


    return cipher.finalize(data)
}