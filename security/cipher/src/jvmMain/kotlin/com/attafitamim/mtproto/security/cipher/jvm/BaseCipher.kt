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

    protected val cipher: Cipher
    protected val cipherMode: Int = mode.toJavaMode()
    protected val keyAlgorithm: String = algorithm.toJavaAlgorithm()

    init {
        val transformation = listOf(
            algorithm.toJavaAlgorithm(),
            algorithmMode.toJavaMode(),
            padding.toJavaPadding()
        ).joinToString(TRANSFORMATION_SEPARATOR)

        cipher = Cipher.getInstance(transformation)
    }

    override fun updateData(data: ByteArray): ByteArray =
        cipher.update(data)

    override fun finalize(data: ByteArray): ByteArray =
        cipher.doFinal(data)

    protected fun CipherMode.toJavaMode() = when (this) {
        CipherMode.ENCRYPT -> Cipher.ENCRYPT_MODE
        CipherMode.DECRYPT -> Cipher.DECRYPT_MODE
    }

    protected fun AlgorithmMode.toJavaMode() = when (this) {
        AlgorithmMode.CTR -> MODE_CTR
        AlgorithmMode.ECB -> MODE_ECB
        AlgorithmMode.GCM -> MODE_GCM
        AlgorithmMode.CBC -> MODE_CBC
    }

    protected fun AlgorithmPadding.toJavaPadding() = when (this) {
        AlgorithmPadding.NONE -> PADDING_NONE
        AlgorithmPadding.PKCS7 -> PADDING_PKCS7
    }

    protected fun Algorithm.toJavaAlgorithm() = when (this) {
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
