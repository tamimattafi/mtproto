package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode

actual object AesCipher : IAesCipherFactory {
    override fun createCipher(
        mode: CipherMode,
        algorithmMode: AlgorithmMode,
        padding: AlgorithmPadding
    ): IAesCipher = TODO()
}
