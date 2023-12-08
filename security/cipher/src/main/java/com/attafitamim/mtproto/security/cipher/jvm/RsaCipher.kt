package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.rsa.IRsaCipher
import com.attafitamim.mtproto.security.cipher.rsa.IRsaCipherFactory


class RsaCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.RSA,
    algorithmMode,
    algorithmPadding
), IRsaCipher {

    override fun init(modulusHex: String, exponentHex: String) {
        TODO("Not yet implemented")
    }

    companion object : IRsaCipherFactory {

        override fun createCipher(
            mode: CipherMode,
            algorithmMode: AlgorithmMode,
            padding: AlgorithmPadding
        ): IRsaCipher = RsaCipher(mode, algorithmMode, padding)
    }
}
