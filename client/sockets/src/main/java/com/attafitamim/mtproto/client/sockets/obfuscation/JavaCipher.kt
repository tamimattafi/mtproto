package com.attafitamim.mtproto.client.sockets.obfuscation

import io.ktor.util.moveToByteArray
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.crypto.cipher.CryptoCipherFactory


class JavaCipher(
    algorithm: String,
    private val keyAlgorithm: String
) : ICipher {

    private val cipher = CryptoCipherFactory.getCryptoCipher(algorithm)

    override fun init(mode: CipherMode, key: ByteArray, iv: ByteArray) {
        val keySpec = SecretKeySpec(key, keyAlgorithm)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(
            mode.toJavaMode(),
            keySpec,
            ivSpec
        )
    }

    override fun updateData(data: ByteArray): ByteArray {
        val inputBuffer = ByteBuffer.wrap(data)
        val outputBuffer = ByteBuffer.allocate(data.size)

        cipher.update(inputBuffer, outputBuffer)

        outputBuffer.flip()
        return outputBuffer.moveToByteArray()
    }

    private fun CipherMode.toJavaMode() = when (this) {
        CipherMode.ENCRYPT -> Cipher.ENCRYPT_MODE
        CipherMode.DECRYPT -> Cipher.DECRYPT_MODE
    }

    companion object {
        const val ALGORITHM_CTR = "AES/CTR/NoPadding"
        const val KEY_ALGORITHM_AES = "AES"
    }
}
