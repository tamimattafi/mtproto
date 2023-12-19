package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

expect class AesGcmCipher(
    mode: CipherMode,
    aesKey: AesKey,
    authLength: Int,
    padding: AlgorithmPadding = AlgorithmPadding.NONE
) : ICipher
