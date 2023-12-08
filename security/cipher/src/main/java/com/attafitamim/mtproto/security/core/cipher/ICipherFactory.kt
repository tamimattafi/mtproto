package com.attafitamim.mtproto.security.core.cipher

import com.attafitamim.mtproto.security.core.cipher.aes.IAesCipher
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.core.cipher.rsa.IRsaCipher

interface ICipherFactory {

    fun createAesCipher(
        mode: CipherMode,
        aesMode: AlgorithmMode,
        padding: AlgorithmPadding = AlgorithmPadding.NONE
    ): IAesCipher

    fun createRsaCipher(
        mode: CipherMode,
        rsaMode: AlgorithmMode,
        padding: AlgorithmPadding = AlgorithmPadding.NONE
    ): IRsaCipher
}
