package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipherFactory

interface IRsaCipherFactory : ICipherFactory {

    override fun createCipher(
        mode: CipherMode,
        algorithmMode: AlgorithmMode,
        padding: AlgorithmPadding
    ): IRsaCipher
}
