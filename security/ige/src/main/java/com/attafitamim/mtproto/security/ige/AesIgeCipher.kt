package com.attafitamim.mtproto.security.ige

import com.attafitamim.mtproto.security.core.cipher.CipherMode
import com.attafitamim.mtproto.security.core.cipher.aes.IAesCipher
import kotlin.experimental.xor

class AesIgeCipher(
    mode: CipherMode
) : IAesCipher {

    private var processedBlocks = 0
    private val engine = AESFastEngine()

    private lateinit var iv: ByteArray
    private lateinit var key: ByteArray

    private val forEncryption = when (mode) {
        CipherMode.ENCRYPT -> true
        CipherMode.DECRYPT -> false
    }

    override fun init(key: ByteArray, iv: ByteArray) {
        this.iv = iv
        this.key = key

        reset()
    }

    override fun updateData(data: ByteArray): ByteArray =
        update(data)

    override fun finalize(data: ByteArray): ByteArray {
        val encryptedData = update(data)
        reset()
        return encryptedData
    }

    private fun reset() {
        processedBlocks = 0
        engine.reset()
        engine.init(forEncryption, key)
    }

    private fun update(
        data: ByteArray
    ): ByteArray {
        val destination = ByteArray(data.size)
        if (forEncryption) {
            encrypt(data, destination, data.size)
        } else {
            decrypt(data, destination, data.size)
        }

        return destination
    }

    private fun decrypt(
        src: ByteArray,
        dest: ByteArray,
        len: Int
    ) {
        val blocksCount = len / 16
        var curIvX = iv
        var curIvY = iv
        var curIvXOffset = 16
        var curIvYOffset = 0

        for (i in 0 until blocksCount) {
            val offset = (processedBlocks + i) * 16

            for (j in 0..15) {
                val processedByte = src[offset + j] xor curIvX[curIvXOffset + j]
                dest[offset + j] = processedByte
            }

            engine.processBlock(dest, offset, dest, offset)
            for (j in 0..15) {
                val processedByte = dest[offset + j] xor curIvY[curIvYOffset + j]
                dest[offset + j] = processedByte
            }

            curIvY = src
            curIvYOffset = offset
            curIvX = dest
            curIvXOffset = offset
        }

        processedBlocks += blocksCount
    }

    private fun encrypt(
        src: ByteArray,
        dest: ByteArray,
        len: Int
    ) {
        val blocksCount = len / 16
        var curIvX = iv
        var curIvY = iv
        var curIvXOffset = 16
        var curIvYOffset = 0
        for (i in 0 until blocksCount) {
            val offset = (processedBlocks + i) * 16

            for (j in 0..15) {
                val processedByte = src[offset + j] xor curIvY[curIvYOffset + j]
                dest[offset + j] = processedByte
            }

            engine.processBlock(dest, offset, dest, offset)
            for (j in 0..15) {
                val processedByte = dest[offset + j] xor curIvX[curIvXOffset + j]
                dest[offset + j] = processedByte
            }

            curIvX = src
            curIvXOffset = offset
            curIvY = dest
            curIvYOffset = offset
        }

        processedBlocks += blocksCount
    }
}
