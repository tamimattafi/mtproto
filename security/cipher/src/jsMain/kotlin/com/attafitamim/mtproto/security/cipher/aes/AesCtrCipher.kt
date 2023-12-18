package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

actual class AesCtrCipher actual constructor(
    mode: CipherMode,
    aesKey: AesKey,
    padding: AlgorithmPadding
) : ICipher {
    override fun updateData(data: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    override fun finalize(data: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }
}