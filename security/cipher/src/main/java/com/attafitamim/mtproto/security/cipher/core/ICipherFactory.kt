package com.attafitamim.mtproto.security.cipher.core

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding

interface ICipherFactory {

    fun createCipher(
        mode: CipherMode,
        algorithmMode: AlgorithmMode,
        padding: AlgorithmPadding = AlgorithmPadding.NONE
    ): ICipher
}
