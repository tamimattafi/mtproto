package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseAesCipher
import javax.crypto.spec.IvParameterSpec

actual class AesCipher actual constructor(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    aesKey: AesKey,
    padding: AlgorithmPadding
) : BaseAesCipher(
    mode,
    algorithmMode,
    padding
), ICipher {

    init {
        val key = createKey(aesKey.key)
        val ivSpec = IvParameterSpec(aesKey.iv)

        platformCipher.init(
            platformCipherMode,
            key,
            ivSpec
        )
    }
}
