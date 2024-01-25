package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseAesCipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

actual class AesGcmCipher actual constructor(
    mode: CipherMode,
    aesKey: AesKey,
    authLength: Int?,
    padding: AlgorithmPadding
) : BaseAesCipher(
    mode,
    AlgorithmMode.GCM,
    padding
), ICipher {

    init {
        val key = createKey(aesKey.key)
        val ivSpec = authLength?.let {
            GCMParameterSpec(authLength, aesKey.iv)
        } ?: IvParameterSpec(aesKey.iv)

        platformCipher.init(
            platformCipherMode,
            key,
            ivSpec
        )
    }
}
