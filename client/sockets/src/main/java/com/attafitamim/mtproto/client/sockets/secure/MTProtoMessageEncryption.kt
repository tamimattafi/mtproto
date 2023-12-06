package com.attafitamim.mtproto.client.sockets.secure

import com.attafitamim.mtproto.client.sockets.obfuscation.toHex
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.AES256IGEEncrypt
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.SHA1
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.SHA256
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.align
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.concat
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.substring
import com.attafitamim.mtproto.client.sockets.utils.serializeData
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.types.TLObject
import com.attafitamim.scheme.mtproto.containers.global.TLMessage
import java.io.IOException
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
            message: TLMessage<out TLObject>
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

            val encryptedData = AES256IGEEncrypt(
                align(unencryptedData, 16),
                aesKeyIvPair.second,
                aesKeyIvPair.first
            )

            println("ENCRYPTED_DATA: ${encryptedData.toHex()}")

            val data =  serializeData(24 + encryptedData.size) {
                writeByteArray(keyId)
                writeByteArray(msgKey)
                writeByteArray(encryptedData)
            }

            return data
        }
/*

        */
/**
         * Decrypt a message following the MTProto description
         * @see [MTProto description](https://core.telegram.org/mtproto/description)

         * @param authKey authKey to use to encrypt
         * @param sessionId session id used
         * @param data message to be decrypted
         * @return decrypted message
         * @throws IOException
         *//*

        @Throws(IOException::class)
        @JvmStatic
        fun decrypt(authKey: AuthKey, sessionId: ByteArray, data: ByteArray): MTMessage {
            val stream = ByteArrayInputStream(data)

            // Retrieve and check authKey
            val msgAuthKeyId = readBytes(8, stream)
            if (!Arrays.equals(authKey.keyId, msgAuthKeyId))
                throw RuntimeException("Message's authKey doesn't match given authKey")

            // Message key
            val msgKey = readBytes(16, stream)
            val aesKeyIvPair = computeAESKeyAndInitVector(authKey, msgKey, 8)

            // Read encrypted data
            val encryptedDataLength = data.size - 24 // Subtract authKey(8) + msgKey(16) length
            val encryptedData = ByteArray(encryptedDataLength)
            readBytes(encryptedData, 0, encryptedDataLength, stream)

            // Decrypt
            val unencryptedData = ByteArray(encryptedDataLength) // AES input/output have the same size
            AES256IGEDecryptBig(encryptedData, unencryptedData, encryptedDataLength, aesKeyIvPair.iv, aesKeyIvPair.key)

            // Decompose
            val unencryptedStream = ByteArrayInputStream(unencryptedData)
            val serverSalt = readBytes(8, unencryptedStream)
            val session = readBytes(8, unencryptedStream)
            // Payload starts here
            val msgId = readLong(unencryptedStream)
            val seqNo = StreamUtils.readInt(unencryptedStream)
            val msgLength = StreamUtils.readInt(unencryptedStream)
            val paddingSize = encryptedDataLength - 32 - msgLength // serverSalt(8) + sessionId(8) + messageId(8) + seqNo(4) + msgLen(4)

            // Security checks
            if (msgId % 2 == 0L) throw SecurityException("Message id of messages sent be the server must be odd, found $msgId")
            if (msgLength % 4 != 0) throw SecurityException("Message length must be a multiple of 4, found $msgLength")
            if (paddingSize > 15 || paddingSize < 0) throw SecurityException("Padding must be between 0 and 15 included, found $paddingSize")
            if (!Arrays.equals(session, sessionId)) throw SecurityException("The message was not intended for this session, expected ${BigInteger(sessionId).toLong()}, found ${BigInteger(session).toLong()}")

            // Read message
            val message = ByteArray(msgLength)
            readBytes(message, 0, msgLength, unencryptedStream)

            val mtMessage = MTMessage(msgId, seqNo, message, message.size)

            // Check that msgKey is equal to the 128 lower-order bits of the SHA1 hash of the previously encrypted portion
            val checkMsgKey = generateMsgKey(serverSalt, session, mtMessage)
            if (!Arrays.equals(checkMsgKey, msgKey))
                throw SecurityException("The message msgKey is inconsistent with it's data")

            return mtMessage
        }
*/

        /**
         * Compute the AES Key and the Initialization Vector

         * @param x x = 0 for messages from client to server and x = 8 for those from server to client.
         * @see [Defining AES Key and Initialization Vector](https://core.telegram.org/mtproto/description.defining-aes-key-and-initialization-vector)
         */
        @JvmStatic
        private fun computeAESKeyAndInitVector(
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