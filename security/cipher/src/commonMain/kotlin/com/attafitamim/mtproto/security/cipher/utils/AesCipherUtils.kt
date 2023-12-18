package com.attafitamim.mtproto.security.cipher.utils

import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.aes.IAesCipherFactory
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode

fun IAesCipherFactory.ctr(
    mode: CipherMode,
    aesKey: AesKey,
    algorithmPadding: AlgorithmPadding = AlgorithmPadding.NONE
) = init(
    mode,
    AlgorithmMode.CTR,
    algorithmPadding,
    aesKey
)

fun IAesCipherFactory.init(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding,
    aesKey: AesKey
) = createCipher(
    mode,
    algorithmMode,
    algorithmPadding
).apply {
    init(aesKey)
}
