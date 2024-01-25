package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

actual class AesGcmCipher actual constructor(
    mode: CipherMode,
    aesKey: AesKey,
    authLength: Int?,
    padding: AlgorithmPadding
) : ICipher {

    // TODO: implement this
    override fun updateData(data: ByteArray): ByteArray =
        data

    override fun finalize(data: ByteArray): ByteArray =
        data
}