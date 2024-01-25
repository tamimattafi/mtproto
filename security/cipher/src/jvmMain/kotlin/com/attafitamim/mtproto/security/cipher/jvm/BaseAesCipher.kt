package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.aes.AesSecretKey
import com.attafitamim.mtproto.security.cipher.aes.EncodedAesSecretKey
import com.attafitamim.mtproto.security.cipher.aes.PlatformAesSecretKey
import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import java.security.Key
import javax.crypto.spec.SecretKeySpec

abstract class BaseAesCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    padding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.AES,
    algorithmMode,
    padding
), ICipher {

    open fun createKey(aesSecretKey: AesSecretKey): Key = when (aesSecretKey) {
        is PlatformAesSecretKey -> aesSecretKey.platformKey
        is EncodedAesSecretKey -> SecretKeySpec(aesSecretKey.encodedBytes, platformAlgorithm)
        else -> error("unsupported AesSecretKey $aesSecretKey")
    }
}
