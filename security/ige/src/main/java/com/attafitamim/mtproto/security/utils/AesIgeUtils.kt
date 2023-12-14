package com.attafitamim.mtproto.security.utils

import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.ige.AesIgeCipher

fun AesIgeCipher.Companion.init(
    mode: CipherMode,
    aesKey: AesKey
) = AesIgeCipher(mode).apply {
    init(aesKey)
}
