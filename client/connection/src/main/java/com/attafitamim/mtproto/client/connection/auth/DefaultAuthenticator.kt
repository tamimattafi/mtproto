package com.attafitamim.mtproto.client.connection.auth

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.align
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.subArray
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.xor
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.manager.ConnectionSession
import com.attafitamim.mtproto.client.connection.session.Session
import com.attafitamim.mtproto.client.connection.utils.SECOND_IN_MILLIS
import com.attafitamim.mtproto.client.connection.utils.convertTimeToMessageId
import com.attafitamim.mtproto.client.connection.utils.parseResponse
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.scheme.types.global.TLClientDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLPQInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLResPQ
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHParams
import com.attafitamim.mtproto.client.scheme.types.global.TLSetClientDHParamsAnswer
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.jvm.RsaCipher
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.attafitamim.mtproto.security.cipher.utils.ecb
import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.digest.jvm.Digest
import com.attafitamim.mtproto.security.digest.utls.sha1
import com.attafitamim.mtproto.security.digest.utls.sha256
import com.attafitamim.mtproto.security.ige.AesIgeCipher
import com.attafitamim.mtproto.security.utils.ISecureRandom
import com.attafitamim.mtproto.security.utils.init
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.utils.calculateData
import com.attafitamim.mtproto.serialization.utils.serializeData
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultAuthenticator(
    private val secureRandom: ISecureRandom,
    private val storage: IAuthenticatorStorage,
    private val serverKeys: List<RsaKey>
) : IAuthenticator {

    private var authCredentials: AuthCredentials? = null
    private val mutex = Mutex()

    override suspend fun generateSession(connectionType: ConnectionType): Session {
        val currentSession = storage.getSession(connectionType)
        if (currentSession != null) {
            return currentSession
        }

        val id = secureRandom.getRandomLong()
        val newSession = Session(id)
        storage.saveSession(connectionType, newSession)
        return newSession
    }

    override suspend fun authenticate(connectionSession: ConnectionSession) {
        mutex.withLock {
            if (authCredentials != null) {
                return
            }

            val currentAuthCredentials = storage.getAuthCredentials()
            if (currentAuthCredentials != null) {
                authCredentials = currentAuthCredentials
                return
            }

            val authResult = connectionSession.connection.generateAuthKey()
            storage.saveAuthCredentials(authResult.credentials)
            authCredentials = authResult.credentials
            connectionSession.session.apply {
                serverTimeDiff = authResult.serverTime - System.currentTimeMillis()
            }
        }
    }

    override suspend fun cleanup() {
        authCredentials = null
    }

    override suspend fun wrapData(session: Session, data: ByteArray): ByteArray {
        val authCredentials = requireNotNull(authCredentials) {
            "Authentication is required to wrap data"
        }

        val encryptedMessage =
            com.attafitamim.mtproto.client.scheme.containers.global.TLEncryptedMessage(
                authCredentials.serverSalt,
                session.id,
                data
            )

        val unencryptedData = serializeData {
            writeLong(authCredentials.serverSalt)
            writeLong(session.id)
            writeByteArray(data)
        }

        val msgKey = generateMsgKey(unencryptedData)

        // Encrypt data
        val aesKey = computeAesKey(authCredentials.key.key, msgKey)
        val encryptedData = AesIgeCipher.init(
            CipherMode.ENCRYPT,
            aesKey,
        ).finalize(align(unencryptedData, 16))


        return serializeData(24 + encryptedData.size) {
            writeByteArray(authCredentials.keyId)
            writeByteArray(msgKey)
            writeByteArray(encryptedData)
        }
    }

    override suspend fun unwrapData(session: Session, data: ByteArray): ByteArray {
        val authCredentials = requireNotNull(authCredentials) {
            "Authentication is required to unwrap data"
        }

        val stream = TLBufferedInputStream
            .Provider(JavaByteBuffer)
            .wrap(data)

        // Retrieve and check authKey
        val size = data.size
        val msgAuthKeyId = stream.readBytes(8)
        if (!authCredentials.keyId.contentEquals(msgAuthKeyId))
            throw RuntimeException("Message's authKey ${authCredentials.keyId.toHex()} doesn't match given authKey ${msgAuthKeyId.toHex()}")

        // Message key
        val msgKey = stream.readBytes(16)
        val aesKeyIvPair = computeAesKey(authCredentials.key.key, msgKey, isOutgoing = false)

        // Read encrypted data
        val encryptedDataLength = size - 24 // Subtract authKey(8) + msgKey(16) length
        val encryptedData = stream.readBytes(encryptedDataLength)

        // Decrypt
        val unencryptedData = AesIgeCipher.init(
            CipherMode.DECRYPT,
            aesKeyIvPair
        ).finalize(encryptedData)

        val unencryptedStream = TLBufferedInputStream.Provider(JavaByteBuffer).wrap(unencryptedData)

        // Decompose
        val serverSalt = unencryptedStream.readLong()
        val sessionId = unencryptedStream.readLong()

        val messageBytes = unencryptedStream.readBytes(encryptedDataLength - 16)

        // Payload starts here
        val paddingSize = encryptedDataLength - 16 - messageBytes.size // serverSalt(8) + sessionId(8) + messageId(8) + seqNo(4) + msgLen(4)

        // Security checks
        if (paddingSize > 15 || paddingSize < 0) throw SecurityException("Padding must be between 0 and 15 included, found $paddingSize")
        if (session.id != sessionId) throw SecurityException("The message was not intended for this session, expected ${session.id}, found $sessionId")

/*
        // Check that msgKey is equal to the 128 lower-order bits of the SHA1 hash of the previously encrypted portion
        val checkMsgKey = generateMsgKey(serverSalt, sessionId, messageBytes)
        if (!Arrays.equals(checkMsgKey, msgKey))
            throw SecurityException("The message msgKey is inconsistent with it's data")
*/

        authCredentials.serverSalt = serverSalt
        return messageBytes
    }

    fun generateMsgKey(
        serverSalt: ByteArray,
        sessionId: Long,
        message: ByteArray
    ): ByteArray? {
        try {
            val crypt = MessageDigest.getInstance("SHA-1")
            crypt.reset()
            crypt.update(serverSalt)
            crypt.update(longToBytes(sessionId))
            crypt.update(message)

            return subArray(crypt.digest(), 4, 16)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return null
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

    private suspend fun IConnection.generateAuthKey(): AuthResult {
        println("CONNECTION: Starting Auth")
        // Step 0
        val resPq = sendReqPQ()
        if (resPq !is TLResPQ.ResPQ) {
            error("resPQ variant not supported $resPq")
        }

        // Step 1
        val newNonce = generateNewNonce()

        // Step 2
        val serverDhParams = sendReqDhParams(resPq, newNonce)
        if (serverDhParams !is TLServerDHParams.ServerDHParamsOk) {
            error("TLServerDHParams variant not supported $serverDhParams")
        }

        // Step 3
        val tempAesCredentials = generateAesKey(
            newNonce,
            resPq.serverNonce
        )

        // Step 4
        val serverDhInner = serverDhParams.getDecryptedData(tempAesCredentials)
        if (serverDhInner !is TLServerDHInnerData.ServerDHInnerData) {
            error("TLServerDHInnerData variant not supported $serverDhInner")
        }

        // Step 5
        val authKey = authCredentials?.key ?: generateAuthKey(serverDhInner)

        repeat(5) { retryId ->
            // Step 6
            val response = sendReqSetDhClientParams(
                resPq,
                tempAesCredentials,
                authKey,
                retryId
            )


            // Step 7
            val authCredentials = response.toAuthCredentials(
                resPq,
                newNonce,
                authKey
            )

            if (authCredentials != null) {
                return AuthResult(
                    authCredentials,
                    serverDhInner.serverTime * SECOND_IN_MILLIS
                )
            }
        }

        error("AUTH_FAILED")
    }

    private suspend fun <T : Any> IConnection.sendRequest(request: TLMethod<T>): T {
        val message = request.toPublicMessage(convertTimeToMessageId(System.currentTimeMillis()))
        println("CONNECTION: auth sending $request")
        sendData(message)
        val rawResponse = listenToData().first()
        val parsedResponse = request.parseResponse(rawResponse)
        println("CONNECTION: auth received $parsedResponse")

        return parsedResponse
    }

    private suspend fun IConnection.sendReqPQ(): TLResPQ {
        val nonceBytes = ByteArray(16)
        SecureRandom().nextBytes(nonceBytes)

        val nonce = com.attafitamim.mtproto.client.scheme.containers.global.TLInt128(nonceBytes)
        val request = com.attafitamim.mtproto.client.scheme.methods.global.TLReqPq(nonce)

        return sendRequest(request)
    }

    private fun generateNewNonce(): com.attafitamim.mtproto.client.scheme.containers.global.TLInt256 {
        val newNonceBytes = ByteArray(32)
        SecureRandom().nextBytes(newNonceBytes)
        return com.attafitamim.mtproto.client.scheme.containers.global.TLInt256(newNonceBytes)
    }

    private suspend fun IConnection.sendReqDhParams(
        resPq: TLResPQ.ResPQ,
        newNonce: com.attafitamim.mtproto.client.scheme.containers.global.TLInt256
    ): TLServerDHParams {
        val serverFingerPrints = resPq.serverPublicKeyFingerprints.toList()
        val rsaKey = serverKeys.firstOrNull { serverKey ->
            serverFingerPrints.contains(serverKey.fingerprint)
        } ?: error("No finger prints from the list are supported by the client: $serverFingerPrints")

        val solvedPQ = PQSolver.solve(BigInteger(1, resPq.pq))

        val solvedP = solvedPQ.p.toByteArray()
        val solvedQ = solvedPQ.q.toByteArray()
        val pqData = TLPQInnerData.PQInnerData(
            resPq.pq,
            solvedP,
            solvedQ,
            resPq.nonce,
            resPq.serverNonce,
            newNonce
        )

        val pqDataBytes = serializeData {
            pqData.serialize(this)
        }

        val pqDataHash = Digest
            .createDigest(DigestMode.SHA1)
            .digest(pqDataBytes)

        val paddingSize = 255 - (pqDataBytes.size + pqDataHash.size)
        val padding = if (paddingSize > 0) Random.nextBytes(paddingSize) else ByteArray(0)
        val dataWithHash = pqDataHash + pqDataBytes + padding

        val encryptedData = RsaCipher.ecb(
            CipherMode.ENCRYPT,
            rsaKey
        ).finalize(dataWithHash)

        val request = com.attafitamim.mtproto.client.scheme.methods.global.TLReqDHParams(
            resPq.nonce,
            resPq.serverNonce,
            solvedP,
            solvedQ,
            rsaKey.fingerprint,
            encryptedData
        )

        return sendRequest(request)
    }

    private fun generateAesKey(
        newNonce: com.attafitamim.mtproto.client.scheme.containers.global.TLInt256,
        serverNonce: com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
    ): AesKey {
        val key = Digest.sha1(
            newNonce.bytes,
            serverNonce.bytes
        ) + Digest.sha1(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(0..<12)


        val iv = Digest.sha1(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(12..<20) + Digest.sha1(
            newNonce.bytes,
            newNonce.bytes
        ) + newNonce.bytes.sliceArray(0..<4)

        return AesKey(key, iv)
    }

    private fun TLServerDHParams.ServerDHParamsOk.getDecryptedData(
        aesKey: AesKey
    ): TLServerDHInnerData {
        val answer = AesIgeCipher.init(
            CipherMode.DECRYPT,
            aesKey
        ).finalize(encryptedAnswer)

        val stream = TLBufferedInputStream
            .Provider(JavaByteBuffer)
            .wrap(answer)

        val answerHash = stream.readBytes(20) // Hash
        val dhInner = TLServerDHInnerData.parse(stream)

        val serializedDhInner = serializeData {
            dhInner.serialize(this)
        }

        val serializedHash = Digest.sha1(serializedDhInner)
        if (!answerHash.contentEquals(serializedHash)) {
            throw SecurityException()
        }

        return dhInner
    }

    private fun generateAuthKey(
        serverDHInnerData: TLServerDHInnerData.ServerDHInnerData,
        size: Int = 256
    ): AuthKey {
        val b = loadBigInt(secureRandom.getRandomBytes(256))
        val g = BigInteger(serverDHInnerData.g.toString())
        val dhPrime = loadBigInt(serverDHInnerData.dhPrime)
        val gb = g.modPow(b, dhPrime)

        val authKeyVal = loadBigInt(serverDHInnerData.gA).modPow(b, dhPrime)
        val authKey =  alignKeyZero(fromBigInt(authKeyVal), size)
        val keyId = subArray(Digest.sha1(authKey), 12, 8)

        val gbBytes = fromBigInt(gb)
        return AuthKey(authKey, keyId, gbBytes)
    }

    private fun fromBigInt(value: BigInteger): ByteArray {
        val res = value.toByteArray()
        return if (res[0].toInt() == 0) {
            val res2 = ByteArray(res.size - 1)
            System.arraycopy(res, 1, res2, 0, res2.size)
            res2
        } else {
            res
        }
    }

    fun alignKeyZero(src: ByteArray, size: Int): ByteArray {
        if (src.size == size) {
            return src
        }
        return if (src.size > size) {
            subArray(
                src,
                src.size - size,
                size
            )
        } else {
            ByteArray(size - src.size) + src
        }
    }

    fun loadBigInt(data: ByteArray?): BigInteger {
        return BigInteger(1, data)
    }

    private suspend fun IConnection.sendReqSetDhClientParams(
        resPq: TLResPQ.ResPQ,
        aesKey: AesKey,
        authKey: AuthKey,
        retryId: Int
    ): TLSetClientDHParamsAnswer {
        val clientDHInner = TLClientDHInnerData.ClientDHInnerData(
            resPq.nonce,
            resPq.serverNonce,
            retryId.toLong(),
            authKey.gb
        )

        val innerDataBytes = serializeData {
            clientDHInner.serialize(this)
        }

        val innerDataWithHash = align(Digest.sha1(innerDataBytes) + innerDataBytes, 16)
        val dataWithHashEnc = AesIgeCipher.init(
            CipherMode.ENCRYPT,
            aesKey
        ).finalize(innerDataWithHash)

        val request = com.attafitamim.mtproto.client.scheme.methods.global.TLSetClientDHParams(
            resPq.nonce,
            resPq.serverNonce,
            dataWithHashEnc
        )
        return sendRequest(request)
    }

    fun readLong(src: ByteArray, offset: Int): Long {
        val a: Long = readUInt(src, offset)
        val b: Long = readUInt(src, offset + 4)
        return (a and 0xFFFFFFFFL) + (b and 0xFFFFFFFFL shl 32)
    }

    fun readUInt(src: ByteArray, offset: Int): Long {
        val a = (src[offset].toInt() and 0xFF).toLong()
        val b = (src[offset + 1].toInt() and 0xFF).toLong()
        val c = (src[offset + 2].toInt() and 0xFF).toLong()
        val d = (src[offset + 3].toInt() and 0xFF).toLong()
        return a + (b shl 8) + (c shl 16) + (d shl 24)
    }

    private fun TLSetClientDHParamsAnswer.toAuthCredentials(
        resPq: TLResPQ.ResPQ,
        newNonce: com.attafitamim.mtproto.client.scheme.containers.global.TLInt256,
        authKey: AuthKey
    ): AuthCredentials? {
        val authAuxHash = Digest.sha1(authKey.key).sliceArray(0 ..< 8)

        return when (this) {
            is TLSetClientDHParamsAnswer.DhGenOk -> {
                val newNonceHash = subArray(Digest.sha1(
                    newNonce.bytes,
                    byteArrayOf(1),
                    authAuxHash
                ),4, 16)

                if (!newNonceHash1.bytes.contentEquals(newNonceHash)) {
                    throw SecurityException()
                }

                val serverSalt = readLong(xor(subArray(newNonce.bytes, 0, 8), subArray(resPq.serverNonce.bytes, 0, 8)), 0)

                val authCredentials = AuthCredentials(authKey, authKey.id, serverSalt)

                println("AUTH_SUCCESS: $authCredentials")

                authCredentials
            }

            is TLSetClientDHParamsAnswer.DhGenRetry -> {
                val newNonceHash = subArray(Digest.sha1(
                    newNonce.bytes,
                    byteArrayOf(2),
                    authAuxHash
                ), 4, 16)

                if (!newNonceHash2.bytes.contentEquals(newNonceHash)) {
                    throw SecurityException()
                }

                null
            }

            is TLSetClientDHParamsAnswer.DhGenFail -> {
                val newNonceHash = subArray(Digest.sha1(
                    newNonce.bytes,
                    byteArrayOf(3),
                    authAuxHash
                ), 4, 16)

                if (!newNonceHash3.bytes.contentEquals(newNonceHash)) {
                    throw SecurityException()
                }

                error("Auth error")
            }
        }
    }

    fun <R : Any> TLMethod<R>.toPublicMessage(
        requestMessageId: Long
    ): ByteArray {
        val authKeyId = 0L
        val methodBytesSize = calculateData(::serialize)
        val methodBytes = serializeData(methodBytesSize, ::serialize)

        return serializeData {
            writeLong(authKeyId)
            writeLong(requestMessageId)
            writeInt(methodBytesSize)
            writeByteArray(methodBytes)
        }
    }

    private fun <T : Any> TLVector<T>.toList() = when (this) {
        is TLVector.Vector -> elements
    }

    fun generateMsgKey(unencryptedData: ByteArray) = subArray(Digest.sha1(unencryptedData), 4, 16)

    fun computeAesKey(
        authKey: ByteArray,
        msgKey: ByteArray,
        isOutgoing: Boolean = true
    ): AesKey {
        val x = if (isOutgoing) 0 else 8
        val a = Digest.sha256(msgKey, subArray(authKey, x, 36))
        val b = Digest.sha256(subArray(authKey, x + 40, 36), msgKey)

        val key = subArray(a, 0, 8) + subArray(b, 8, 16) + subArray(a, 24, 8)
        val iv = subArray(b, 0, 8) + subArray(a, 8, 16) + subArray(b, 24, 8)

        return AesKey(
            key,
            iv
        )
    }
}