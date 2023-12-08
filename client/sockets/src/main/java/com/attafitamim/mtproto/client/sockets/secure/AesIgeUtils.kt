package com.attafitamim.mtproto.client.sockets.secure

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.ige.AesIgeCipher


fun decryptAesIge(
    key: ByteArray,
    iv: ByteArray,
    data: ByteArray
): ByteArray {
    /*val res = ByteArray(data.size)
    DefaultAESImplementation().AES256IGEDecrypt(
        data,
        res,
        data.size,
        iv,
        key
    )
    return res*/
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
/*
    val res = ByteArray(data.size)


    DefaultAESImplementation().AES256IGEEncrypt(
        CryptoUtils.align(data, 16),
        res,
        data.size,
        iv,
        key
    )*/
}