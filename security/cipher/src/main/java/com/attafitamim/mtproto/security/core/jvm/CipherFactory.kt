package com.attafitamim.mtproto.security.core.jvm

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.core.cipher.ICipherFactory
import com.attafitamim.mtproto.security.core.cipher.aes.IAesCipher
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.core.cipher.rsa.IRsaCipher

class CipherFactory : ICipherFactory {

    override fun createAesCipher(
        mode: CipherMode,
        aesMode: AlgorithmMode,
        padding: AlgorithmPadding
    ): IAesCipher = AesCipher(mode, aesMode, padding)

    override fun createRsaCipher(
        mode: CipherMode,
        rsaMode: AlgorithmMode,
        padding: AlgorithmPadding
    ): IRsaCipher = RsaCipher(mode, rsaMode, padding)
}
