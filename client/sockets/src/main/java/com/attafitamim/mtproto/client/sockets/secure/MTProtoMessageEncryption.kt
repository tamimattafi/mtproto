package com.attafitamim.mtproto.client.sockets.secure

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.client.sockets.utils.toHex
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.SHA1
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.SHA256
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.align
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.concat
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.substring
import com.attafitamim.mtproto.client.sockets.utils.serializeData
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.scheme.mtproto.containers.global.TLMessage
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Helper class to handle the encryption/decryption of message following the MTProto protocol description
 */
class MTProtoMessageEncryption {

    companion object {
        /**
         * Generate message key
         * The lower-order 128 bits of the SHA1 hash of the part of the message to be encrypted (including the internal header and excluding the alignment bytes).
         * @param unencryptedData concatenation of the data to be encrypted (without padding)
         * @return message key
         * @see [Message Key](https://core.telegram.org/mtproto/description.message-key)
         */
        @JvmStatic fun generateMsgKey(unencryptedData: ByteArray) = substring(SHA1(unencryptedData), 4, 16)


        /**
         * Generate message key for the given message
         * @param serverSalt server salt used
         * @param sessionId session id used
         * @param message concerned message
         * @return The lower-order 128 bits of the SHA1 hash of the part of the message to be encrypted
         * @see [Message Key](https://core.telegram.org/mtproto/description.message-key)
         */
        @JvmStatic fun generateMsgKey(
            serverSalt: ByteArray,
            sessionId: ByteArray,
            message: TLMessage<out TLSerializable>
        ): ByteArray? {
            try {
                val crypt = MessageDigest.getInstance("SHA-1")
                crypt.reset()
                crypt.update(serverSalt)
                crypt.update(sessionId)
                crypt.update(longToBytes(message.msgId))
                crypt.update(intToBytes(message.seqno))
                crypt.update(intToBytes(message.bytes))

                val payload = serializeData {
                    message.body.serialize(this)
                }

                crypt.update(payload, 0, message.bytes) // Use len as payload may have padding
                return substring(crypt.digest(), 4, 16)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return null
        }

        fun intToBytes(value: Int): ByteArray? {
            return byteArrayOf(
                (value and 0xFF).toByte(),
                (value shr 8 and 0xFF).toByte(),
                (value shr 16 and 0xFF).toByte(),
                (value shr 24 and 0xFF).toByte()
            )
        }

        fun longToBytes(value: Long): ByteArray? {
            return byteArrayOf(
                (value and 0xFFL).toByte(),
                (value shr 8 and 0xFFL).toByte(),
                (value shr 16 and 0xFFL).toByte(),
                (value shr 24 and 0xFFL).toByte(),
                (value shr 32 and 0xFFL).toByte(),
                (value shr 40 and 0xFFL).toByte(),
                (value shr 48 and 0xFFL).toByte(),
                (value shr 56 and 0xFFL).toByte()
            )
        }

        /**
         * Encrypt a message following the MTProto description
         * @see [MTProto description](https://core.telegram.org/mtproto/description)

         * @param authKey authKey to use to encrypt
         * @param sessionId session id used
         * @param serverSalt server salt used
         * @param message message to be encrypted
         * @return encrypted message containing the encryption and the origin message
         * @throws IOException
         */
        @OptIn(ExperimentalStdlibApi::class)
        @Throws(IOException::class)
        @JvmStatic
        fun <T : TLSerializable> encrypt(
            authKey: ByteArray,
            keyId: ByteArray,
            sessionId: ByteArray,
            serverSalt: Long,
            message: TLMessage<T>
        ): ByteArray {
            println("SERVER_SALT: ${serverSalt.toHexString()}")
            println("SESSION_ID: ${sessionId.toHex()}")
            println("AUTH_KEY_ID: ${keyId.toHex()}")
            println("AUTH_KEY: ${authKey.toHex()}")

            // Build message body
            val unencryptedData = serializeData {
                writeLong(serverSalt)
                writeByteArray(sessionId)
                writeLong(message.msgId)
                writeInt(message.seqno)
                writeInt(message.bytes)
                message.body.serialize(this)
            }
            println("RAW_DATA: ${unencryptedData.toHex()}")

            val msgKey = generateMsgKey(unencryptedData)
            println("MSG_KEY: ${msgKey.toHex()}")

            // Encrypt data
            val aesKeyIvPair = computeAESKeyAndInitVector(authKey, msgKey)
            println("AES_KEY: ${aesKeyIvPair.first.toHex()}")
            println("AES_IV: ${aesKeyIvPair.second.toHex()}")

            val encryptedData = encryptAesIge(
                aesKeyIvPair.first,
                aesKeyIvPair.second,
                align(unencryptedData, 16)
            )

            println("ENCRYPTED_DATA: ${encryptedData.toHex()}")

            val data =  serializeData(24 + encryptedData.size) {
                writeByteArray(keyId)
                writeByteArray(msgKey)
                writeByteArray(encryptedData)
            }

            return data
        }

        /**
         * Decrypt a message following the MTProto description
         * @see [MTProto description](https://core.telegram.org/mtproto/description)

         * @param authKey authKey to use to encrypt
         * @param sessionId session id used
         * @param data message to be decrypted
         * @return decrypted message
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmStatic
        inline fun <reified T : TLSerializable> decrypt(
            authKey: ByteArray,
            keyId: ByteArray,
            sessionId: ByteArray,
            data: ByteArray,
            parseT: (inputStream: TLInputStream) -> T
        ): TLMessage<T> {
            val stream = TLBufferedInputStream
                .Provider(JavaByteBuffer)
                .wrap(data)

            // Retrieve and check authKey
            val size = stream.readInt()
            val msgAuthKeyId = stream.readBytes(8)
            if (!keyId.contentEquals(msgAuthKeyId))
                throw RuntimeException("Message's authKey ${keyId.toHex()} doesn't match given authKey ${msgAuthKeyId.toHex()}")

            // Message key
            val msgKey = stream.readBytes(16)
            val aesKeyIvPair = computeAESKeyAndInitVector(authKey, msgKey, isOutgoing = false)

            // Read encrypted data
            val encryptedDataLength = size - 24 // Subtract authKey(8) + msgKey(16) length
            val encryptedData = stream.readBytes(encryptedDataLength)

            // Decrypt
            val unencryptedData = decryptAesIge(aesKeyIvPair.first, aesKeyIvPair.second, encryptedData)

            val unencryptedStream = TLBufferedInputStream.Provider(JavaByteBuffer).wrap(unencryptedData)


            // Decompose
            val serverSalt = unencryptedStream.readBytes(8)
            val session = unencryptedStream.readBytes(8)
            // Payload starts here
            val mtMessage = TLMessage.parse(unencryptedStream, parseT)
            val paddingSize = encryptedDataLength - 32 - mtMessage.bytes // serverSalt(8) + sessionId(8) + messageId(8) + seqNo(4) + msgLen(4)

            // Security checks
            if (mtMessage.msgId % 2 == 0L) throw SecurityException("Message id of messages sent be the server must be odd, found ${mtMessage.msgId}")
            if (mtMessage.bytes % 4 != 0) throw SecurityException("Message length must be a multiple of 4, found ${mtMessage.bytes}")
            if (paddingSize > 15 || paddingSize < 0) throw SecurityException("Padding must be between 0 and 15 included, found $paddingSize")
            if (!Arrays.equals(session, sessionId)) throw SecurityException("The message was not intended for this session, expected ${BigInteger(sessionId).toLong()}, found ${BigInteger(session).toLong()}")

            // Check that msgKey is equal to the 128 lower-order bits of the SHA1 hash of the previously encrypted portion
            val checkMsgKey = generateMsgKey(serverSalt, session, mtMessage)
            if (!Arrays.equals(checkMsgKey, msgKey))
                throw SecurityException("The message msgKey is inconsistent with it's data")

            return mtMessage
        }


        @Throws(IOException::class)
        fun readInt(stream: InputStream): Int {
            val a = stream.read()
            if (a < 0) throw IOException()
            val b = stream.read()
            if (b < 0) throw IOException()
            val c = stream.read()
            if (c < 0) throw IOException()
            val d = stream.read()
            if (d < 0) throw IOException()
            return a + (b shl 8) + (c shl 16) + (d shl 24)
        }

        @Throws(IOException::class)
        fun readUInt(stream: InputStream): Long {
            val a = stream.read().toLong()
            if (a < 0) {
                throw IOException()
            }
            val b = stream.read().toLong()
            if (b < 0) {
                throw IOException()
            }
            val c = stream.read().toLong()
            if (c < 0) {
                throw IOException()
            }
            val d = stream.read().toLong()
            if (d < 0) {
                throw IOException()
            }
            return a + (b shl 8) + (c shl 16) + (d shl 24)
        }

        @Throws(IOException::class)
        fun readLong(stream: InputStream): Long {
            val a = readUInt(stream)
            val b = readUInt(stream)
            return a + (b shl 32)
        }
        fun readUInt(src: ByteArray, offset: Int): Long {
            val a = (src[offset].toInt() and 0xFF).toLong()
            val b = (src[offset + 1].toInt() and 0xFF).toLong()
            val c = (src[offset + 2].toInt() and 0xFF).toLong()
            val d = (src[offset + 3].toInt() and 0xFF).toLong()
            return a + (b shl 8) + (c shl 16) + (d shl 24)
        }

        fun readLong(src: ByteArray, offset: Int): Long {
            val a: Long = readUInt(src, offset)
            val b: Long = readUInt(src, offset + 4)
            return (a and 0xFFFFFFFFL) + (b and 0xFFFFFFFFL shl 32)
        }

        /**
         * Compute the AES Key and the Initialization Vector

         * @param x x = 0 for messages from client to server and x = 8 for those from server to client.
         * @see [Defining AES Key and Initialization Vector](https://core.telegram.org/mtproto/description.defining-aes-key-and-initialization-vector)
         */
        @JvmStatic
        fun computeAESKeyAndInitVector(
            authKey: ByteArray,
            msgKey: ByteArray,
            isOutgoing: Boolean = true
        ): Pair<ByteArray, ByteArray> {
            val x = if (isOutgoing) 0 else 8
            val a = SHA256(msgKey, substring(authKey, x, 36))
            val b = SHA256(substring(authKey, x + 40, 36), msgKey)

            val aesKey = concat(substring(a, 0, 8), substring(b, 8, 16), substring(a, 24, 8))
            val aesIv = concat(substring(b, 0, 8), substring(a, 8, 16), substring(b, 24, 8))

            return aesKey to aesIv
        }
    }
}