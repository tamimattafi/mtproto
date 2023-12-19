package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseCipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class AesGcmCipher actual constructor(
    mode: CipherMode,
    aesKey: AesKey,
    authLength: Int,
    padding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.AES,
    AlgorithmMode.GCM,
    padding
), ICipher {

    init {
        val keySpec = SecretKeySpec(aesKey.key, keyAlgorithm)
        val ivSpec = GCMParameterSpec(authLength, aesKey.iv)

        cipher.init(
            cipherMode,
            keySpec,
            ivSpec
        )
    }
}
