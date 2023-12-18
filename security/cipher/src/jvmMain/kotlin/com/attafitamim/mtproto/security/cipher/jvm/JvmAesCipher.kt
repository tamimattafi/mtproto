package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.aes.IAesCipher
import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class JvmAesCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding
) : JvmBaseCipher(
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

    override fun init(aesKey: AesKey) {
        init(aesKey.key, aesKey.iv)
    }
}
