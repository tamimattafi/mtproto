package com.attafitamim.mtproto.security.core.jvm

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.core.cipher.ICipher
import com.attafitamim.mtproto.security.core.cipher.algorithm.Algorithm
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.core.cipher.algorithm.AlgorithmPadding
import javax.crypto.Cipher

abstract class BaseCipher(
    mode: CipherMode,
    algorithm: Algorithm,
    algorithmMode: AlgorithmMode,
    padding: AlgorithmPadding
) : ICipher {

    protected val cipher: Cipher
    protected val cipherMode: Int = mode.toJavaMode()

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
    }

    protected fun AlgorithmPadding.toJavaPadding() = when (this) {
        AlgorithmPadding.NONE -> PADDING_NONE
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

        // Paddings
        const val PADDING_NONE = "NoPadding"
    }
}
