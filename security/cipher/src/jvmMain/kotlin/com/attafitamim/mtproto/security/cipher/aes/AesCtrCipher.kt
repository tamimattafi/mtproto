package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseCipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class AesCtrCipher actual constructor(
    mode: CipherMode,
    aesKey: AesKey,
    padding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.AES,
    AlgorithmMode.CTR,
    padding
), ICipher {

    init {
        val keySpec = SecretKeySpec(aesKey.key, Algorithm.AES.toJavaAlgorithm())
        val ivSpec = IvParameterSpec(aesKey.iv)

        cipher.init(
            cipherMode,
            keySpec,
            ivSpec
        )
    }
}
