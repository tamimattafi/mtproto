package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher

expect class RsaEcbCipher(
    mode: CipherMode,
    rsaKey: RsaKey,
    padding: AlgorithmPadding = AlgorithmPadding.NONE
) : ICipher