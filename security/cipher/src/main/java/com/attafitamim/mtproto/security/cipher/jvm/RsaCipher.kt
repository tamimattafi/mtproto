package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.rsa.IRsaCipher
import com.attafitamim.mtproto.security.cipher.rsa.IRsaCipherFactory
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher


class RsaCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    algorithmPadding: AlgorithmPadding
) : BaseCipher(
    mode,
    Algorithm.RSA,
    algorithmMode,
    algorithmPadding
), IRsaCipher {

    override fun init(modulusHex: String, exponentHex: String) {
        val keyFactory = KeyFactory.getInstance(ALGORITHM_RSA)

        val modulus = BigInteger(modulusHex, 16)
        val exponent = BigInteger(exponentHex, 16)
        val keySpec = RSAPublicKeySpec(modulus, exponent)
        val publicKey = keyFactory.generatePublic(keySpec)

        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    }

    override fun init(rsaKey: RsaKey) {
        init(rsaKey.modulusHex, rsaKey.exponentHex)
    }

    companion object : IRsaCipherFactory {

        override fun createCipher(
            mode: CipherMode,
            algorithmMode: AlgorithmMode,
            padding: AlgorithmPadding
        ): IRsaCipher = RsaCipher(mode, algorithmMode, padding)
    }
}
