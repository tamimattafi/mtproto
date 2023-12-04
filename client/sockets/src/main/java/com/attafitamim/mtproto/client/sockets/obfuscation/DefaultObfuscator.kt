package com.attafitamim.mtproto.client.sockets.obfuscation

import com.attafitamim.mtproto.client.sockets.buffer.IByteBufferProvider
import com.attafitamim.mtproto.client.sockets.obfuscation.cipher.CipherMode
import com.attafitamim.mtproto.client.sockets.obfuscation.cipher.ICipher
import com.attafitamim.mtproto.client.sockets.obfuscation.cipher.ICipherFactory

/**
 * The default obfuscator, check telegram docs for more info:
 * https://core.telegram.org/mtproto/mtproto-transports#transport-obfuscation
 *
 * @property secureRandom A randomizer that gives a reliable result
 */
class DefaultObfuscator(
    private val secureRandom: ISecureRandom,
    private val bufferProvider: IByteBufferProvider,
    private val cipherProvider: ICipherFactory,
    private val protocol: Int = DEFAULT_PROTOCOL,
    private val dataCenter: Int = DEFAULT_DATA_CENTER
) : IObfuscator {

    private var encryptionCipher: ICipher? = null
    private var decryptionCipher: ICipher? = null

    override fun init(): ByteArray {
        val initBytes = generateInitBytes()
        println("INIT: ${initBytes.toHex()}")

        val encryptionKey = initBytes.getKey()
        val encryptionIV = initBytes.getIV()
        println("INIT_KEY: ${encryptionKey.toHex()}")
        println("INIT_IV: ${encryptionIV.toHex()}")

        val encryptionCipher = cipherProvider.createAESCTRCipher().apply {
            init(CipherMode.ENCRYPT, encryptionKey, encryptionIV)
        }

        val initBytesReversed = initBytes.reversedArray()
        val decryptionKey = initBytesReversed.getKey()
        val decryptionIV = initBytesReversed.getIV()
        val decryptionCipher = cipherProvider.createAESCTRCipher().apply {
            init(CipherMode.DECRYPT, decryptionKey, decryptionIV)
        }

        val encryptedInitBytes = encryptionCipher.updateData(initBytes)
        println("ENCRYPTED_INIT: ${encryptedInitBytes.toHex()}")

        val encryptedFooter = encryptedInitBytes.sliceArray(PROTOCOL_POSITION..< initBytes.size)

        this.encryptionCipher = encryptionCipher
        this.decryptionCipher = decryptionCipher

        return initBytes.sliceArray(0..< PROTOCOL_POSITION) + encryptedFooter
    }

    override fun obfuscate(data: ByteArray): ByteArray {
        val encryptionCipher = requireNotNull(encryptionCipher) {
            "Obfuscator not initialized"
        }

        return encryptionCipher.updateData(data)
    }

    override fun clarify(data: ByteArray): ByteArray {
        val decryptionCipher = requireNotNull(decryptionCipher) {
            "Obfuscator not initialized"
        }

        return decryptionCipher.updateData(data)
    }

    private fun generateInitBytes(): ByteArray {
        while (true) {
            val randomBytes = secureRandom.getRandomBytes(RANDOM_BYTES_SIZE)
            val bytesBuffer = bufferProvider.wrap(randomBytes)

            val firstByte = bytesBuffer.getByte()
            if (prohibitedFirstBytes.contains(firstByte)) {
                continue
            }

            bytesBuffer.rewind()
            val firstInt = bytesBuffer.getInt()
            if (prohibitedFirstInts.contains(firstInt)) {
                continue
            }

            val secondInt = bytesBuffer.getInt()
            if (prohibitedSecondInts.contains(secondInt)) {
                continue
            }

            bytesBuffer.position = PROTOCOL_POSITION
            bytesBuffer.putInt(protocol)
            bytesBuffer.putShort(dataCenter.toShort())

            bytesBuffer.rewind()
            return bytesBuffer.getByteArray()
        }
    }

    private fun ByteArray.getKey(): ByteArray = sliceArray(8..< 40)
    private fun ByteArray.getIV(): ByteArray = sliceArray(40..< PROTOCOL_POSITION)

    private companion object {

        const val DEFAULT_PROTOCOL: Int = 0xeeeeeeee.toInt()
        const val DEFAULT_DATA_CENTER: Int = 0xfcff

        private const val RANDOM_BYTES_SIZE = 64
        private const val PROTOCOL_POSITION = 56

        private val prohibitedFirstBytes: Set<Byte> = hashSetOf(
            0xef.toByte()
        )

        private val prohibitedFirstInts: Set<Int> = hashSetOf(
            0x44414548,
            0x54534f50,
            0x20544547,
            0x4954504f,
            0xdddddddd.toInt(),
            0xeeeeeeee.toInt(),
            0x02010316
        )

        private val prohibitedSecondInts: Set<Int> = hashSetOf(
            0x00000000
        )
    }
}