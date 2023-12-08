package com.attafitamim.mtproto.security.core.jvm

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.core.cipher.aes.IAesCipher
import com.attafitamim.mtproto.security.core.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmPadding
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class AesCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.AES,
    algorithmMode,
    algorithmPadding
), IAesCipher {

    override fun init(key: ByteArray, iv: ByteArray) {
        val keySpec = SecretKeySpec(key, Algorithm.AES.toJavaAlgorithm())
        val ivSpec = IvParameterSpec(iv)

        cipher.init(
            cipherMode,
            keySpec,
            ivSpec
        )
    }
}
