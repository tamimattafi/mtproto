package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

expect class AesCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    aesKey: AesKey,
    padding: AlgorithmPadding = AlgorithmPadding.NONE
) : ICipher
