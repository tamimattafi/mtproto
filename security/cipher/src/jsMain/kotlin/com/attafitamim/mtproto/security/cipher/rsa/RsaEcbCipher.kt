package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

actual class RsaEcbCipher actual constructor(
    mode: CipherMode,
    rsaKey: RsaKey,
    padding: AlgorithmPadding
) : ICipher {
    override fun updateData(data: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    override fun finalize(data: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }
}
