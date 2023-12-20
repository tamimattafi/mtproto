package com.attafitamim.mtproto.security.obfuscation

import com.attafitamim.mtproto.buffer.core.ByteBuffer
import com.attafitamim.mtproto.security.cipher.aes.AesCipher
import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.utils.SecureRandom
import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The default obfuscator, check telegram docs for more info:
 * https://core.telegram.org/mtproto/mtproto-transports#transport-obfuscation
 *
 */
class DefaultObfuscator(
    private val protocol: Int = DEFAULT_PROTOCOL,
    private val dataCenter: Int = DEFAULT_DATA_CENTER
) : IObfuscator {

    @Volatile
    private var encryptionCipher: ICipher? = null

    @Volatile
    private var decryptionCipher: ICipher? = null

    private var mutex = Mutex()

    override suspend fun init(): ByteArray = mutex.withLock {
        release()

        val initBytes = generateInitBytes()

        val encryptionKey = initBytes.getAesKey()
        val encryptionCipher = AesCipher(
            CipherMode.ENCRYPT,
            AlgorithmMode.CTR,
            encryptionKey
        )

        val initBytesReversed = initBytes.reversedArray()
        val decryptionKey = initBytesReversed.getAesKey()
        val decryptionCipher = AesCipher(
            CipherMode.DECRYPT,
            AlgorithmMode.CTR,
            decryptionKey
        )

        val encryptedInitBytes = encryptionCipher.updateData(initBytes)

        val encryptedFooter = encryptedInitBytes.sliceArray(PROTOCOL_POSITION..< initBytes.size)

        this.encryptionCipher = encryptionCipher
        this.decryptionCipher = decryptionCipher

        return initBytes.sliceArray(0..< PROTOCOL_POSITION) + encryptedFooter
    }

    override suspend fun obfuscate(data: ByteArray): ByteArray = mutex.withLock {
        val encryptionCipher = requireNotNull(encryptionCipher) {
            "Obfuscator not initialized"
        }

        return encryptionCipher.updateData(data)
    }

    override suspend fun clarify(data: ByteArray): ByteArray = mutex.withLock {
        val decryptionCipher = requireNotNull(decryptionCipher) {
            "Obfuscator not initialized"
        }

        return decryptionCipher.updateData(data)
    }

    override suspend fun release() {
        decryptionCipher = null
        encryptionCipher = null
    }

    override suspend fun isInitialized() =
        decryptionCipher != null && encryptionCipher != null

    private fun generateInitBytes(): ByteArray {
        while (true) {
            val randomBytes = SecureRandom().getRandomBytes(RANDOM_BYTES_SIZE)
            val bytesBuffer = ByteBuffer.wrap(randomBytes)

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

    private fun ByteArray.getAesKey(): AesKey {
        val key = getKey()
        val iv = getIV()

        return AesKey(key, iv)
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
