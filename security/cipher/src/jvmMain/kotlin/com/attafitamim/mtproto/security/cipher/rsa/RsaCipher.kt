package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.jvm.JvmRsaCipher

actual object RsaCipher : IRsaCipherFactory {

    override fun createCipher(
        mode: CipherMode,
        algorithmMode: AlgorithmMode,
        padding: AlgorithmPadding
    ): IRsaCipher = JvmRsaCipher(mode, algorithmMode, padding)
}
