package com.attafitamim.mtproto.security.core.jvm

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.core.cipher.rsa.IRsaCipher


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
}
