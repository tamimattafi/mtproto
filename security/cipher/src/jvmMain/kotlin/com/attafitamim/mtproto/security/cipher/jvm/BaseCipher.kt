package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import javax.crypto.Cipher

abstract class BaseCipher(
    mode: CipherMode,
    algorithm: Algorithm,
    algorithmMode: AlgorithmMode,
    padding: AlgorithmPadding
) : ICipher {

    protected val platformCipher: Cipher
    protected val platformCipherMode: Int = mode.toPlatform()
    protected val platformAlgorithm: String = algorithm.toPlatform()
    protected val platformAlgorithmMode: String = algorithmMode.toPlatform()
    protected val platformPadding: String = padding.toPlatform()

    init {
        val transformation = listOf(
            platformAlgorithm,
            platformAlgorithmMode,
            platformPadding
        ).joinToString(TRANSFORMATION_SEPARATOR)

        platformCipher = Cipher.getInstance(transformation)
    }

    override fun updateData(data: ByteArray): ByteArray =
        platformCipher.update(data)

    override fun finalize(data: ByteArray): ByteArray =
        platformCipher.doFinal(data)

    protected fun CipherMode.toPlatform() = when (this) {
        CipherMode.ENCRYPT -> Cipher.ENCRYPT_MODE
        CipherMode.DECRYPT -> Cipher.DECRYPT_MODE
    }

    protected fun AlgorithmMode.toPlatform() = when (this) {
        AlgorithmMode.CTR -> MODE_CTR
        AlgorithmMode.ECB -> MODE_ECB
        AlgorithmMode.GCM -> MODE_GCM
        AlgorithmMode.CBC -> MODE_CBC
    }

    protected fun AlgorithmPadding.toPlatform() = when (this) {
        AlgorithmPadding.NONE -> PADDING_NONE
        AlgorithmPadding.PKCS7 -> PADDING_PKCS7
    }

    protected fun Algorithm.toPlatform() = when (this) {
        Algorithm.AES -> ALGORITHM_AES
        Algorithm.RSA -> ALGORITHM_RSA
    }

    companion object {
        const val TRANSFORMATION_SEPARATOR = "/"

        // Algorithms
        const val ALGORITHM_AES = "AES"
        const val ALGORITHM_RSA = "RSA"

        // Modes
        const val MODE_CTR = "CTR"
        const val MODE_ECB = "ECB"
        const val MODE_GCM = "GCM"
        const val MODE_CBC = "CBC"

        // Paddings
        const val PADDING_NONE = "NoPadding"
        const val PADDING_PKCS7 = "PKCS7Padding"
    }
}
