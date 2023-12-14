package com.attafitamim.mtproto.security.cipher.utils

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.rsa.IRsaCipherFactory
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey

fun IRsaCipherFactory.ecb(
    mode: CipherMode,
    rsaKey: RsaKey,
    algorithmPadding: AlgorithmPadding = AlgorithmPadding.NONE
) = init(
    mode,
    AlgorithmMode.ECB,
    algorithmPadding,
    rsaKey
)

fun IRsaCipherFactory.init(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding,
    rsaKey: RsaKey
) = createCipher(
    mode,
    algorithmMode,
    algorithmPadding
).apply {
    init(rsaKey)
}