package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.ios.BaseAesCipher
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.CoreCrypto.CCCryptorCreateWithMode

@OptIn(ExperimentalForeignApi::class)
actual class AesCipher actual constructor(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    aesKey: AesKey,
    padding: AlgorithmPadding
) : BaseAesCipher(
    mode,
    algorithmMode,
    padding,
    aesKey
), ICipher {

    init {
        val status = CCCryptorCreateWithMode(
            op = platformCipherMode,
            mode = platformAlgorithmMode,
            alg = platformAlgorithm,
            padding = platformPadding,
            iv = ivRef,
            key = keyRef,
            keyLength = keySize,
            tweak = tweak,
            tweakLength = tweakLength,
            numRounds = numRounds,
            options = cipherOptions,
            cryptorRef = platformCipher.ptr
        )

        handleStatus(status)
    }
}