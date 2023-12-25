package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.jvm.BaseCipher
import com.attafitamim.mtproto.security.cipher.utils.toDERFormat
import java.math.BigInteger
import java.security.Key
import java.security.KeyFactory
import java.security.spec.RSAPrivateKeySpec
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

    private val keyFactory: KeyFactory get() =
        KeyFactory.getInstance(platformAlgorithm)

    init {
        val key = rsaKey.toPlatform()
        platformCipher.init(platformCipherMode, key)
    }

    private fun RsaKey.toPlatform(): Key = when (this) {
        is RsaKey.Raw -> {
            toDERFormat()
            toPlatform()
        }
    }

    private fun RsaKey.Raw.toPlatform(): Key {
        val modulus = BigInteger(modulusHex, KEY_BASE)
        val exponent = BigInteger(exponentHex, KEY_BASE)

        return when (type) {
            RsaKey.Type.PUBLIC -> {
                val keySpec = RSAPublicKeySpec(modulus, exponent)
                keyFactory.generatePublic(keySpec)
            }

            RsaKey.Type.PRIVATE -> {
                val keySpec = RSAPrivateKeySpec(modulus, exponent)
                keyFactory.generatePrivate(keySpec)
            }
        }
    }

    private companion object {
        const val KEY_BASE = 16
    }
}