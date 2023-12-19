package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseCipher
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec

actual class RsaEcbCipher actual constructor(
    mode: CipherMode,
    rsaKey: RsaKey,
    padding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.RSA,
    AlgorithmMode.ECB,
    padding
), ICipher {

    init {
        val keyFactory = KeyFactory.getInstance(keyAlgorithm)

        val modulus = BigInteger(rsaKey.modulusHex, KEY_BASE)
        val exponent = BigInteger(rsaKey.exponentHex, KEY_BASE)
        val keySpec = RSAPublicKeySpec(modulus, exponent)
        val publicKey = keyFactory.generatePublic(keySpec)

        cipher.init(cipherMode, publicKey)
    }

    private companion object {
        const val KEY_BASE = 16
    }
}