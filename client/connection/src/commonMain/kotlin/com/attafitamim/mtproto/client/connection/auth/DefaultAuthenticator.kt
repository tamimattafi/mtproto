package com.attafitamim.mtproto.client.connection.auth

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.align
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.subArray
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils.xor
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.session.Session
import com.attafitamim.mtproto.client.connection.utils.SECOND_IN_MILLIS
import com.attafitamim.mtproto.client.connection.utils.convertTimeToMessageId
import com.attafitamim.mtproto.client.connection.utils.parseResponse
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.scheme.containers.global.TLEncryptedMessage
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt256
import com.attafitamim.mtproto.client.scheme.methods.global.TLReqDHParams
import com.attafitamim.mtproto.client.scheme.types.global.TLClientDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLPQInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLResPQ
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHParams
import com.attafitamim.mtproto.client.scheme.types.global.TLSetClientDHParamsAnswer
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.security.cipher.aes.AesIgeCipher
import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.rsa.RsaEcbCipher
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.attafitamim.mtproto.security.digest.core.Digest
import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.utils.SecureRandom
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.utils.calculateData
import com.attafitamim.mtproto.serialization.utils.serializeData
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class DefaultAuthenticator(
    private val storage: IAuthenticatorStorage,
    private val serverKeys: List<RsaKey>
) : IAuthenticator {

    private var authCredentials: AuthCredentials? = null
    private val mutex = Mutex()
    private val secureRandom = SecureRandom()

    override suspend fun authenticate(
        connectionType: ConnectionType,
        connection: IConnection
    ): Session = mutex.withLock {
        val authCredentials = authenticate(connection)
        val currentSession = storage.getSession(connectionType)
        if (currentSession != null) {
            return currentSession
        }

        val id = secureRandom.getRandomLong()
        val authKeyId = authCredentials.keyId.toHex(appendSpaces = false)
        val newSession = Session(id, authKeyId)
        storage.saveSession(connectionType, newSession)
        return newSession
    }

    private suspend fun authenticate(connection: IConnection): AuthCredentials {
        val currentAuthCredentials = authCredentials
        if (currentAuthCredentials != null) {
            return currentAuthCredentials
        }

        val savedAuthCredentials = storage.getAuthCredentials()
        if (savedAuthCredentials != null) {
            authCredentials = savedAuthCredentials
            return savedAuthCredentials
        }

        val authResult = connection.generateAuthKey()
        storage.saveAuthCredentials(authResult.credentials)
        authCredentials = authResult.credentials

        return authResult.credentials
    }

    override suspend fun cleanup() = mutex.withLock {
        authCredentials = null
    }

    override suspend fun wrapData(session: Session, data: ByteArray): ByteArray {
        val authCredentials = requireAuthCredentials()

        val encryptedMessage = TLEncryptedMessage(
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
        val encryptedData = AesIgeCipher(
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
        val authCredentials = requireAuthCredentials()

        val stream = TLBufferedInputStream.wrap(data)

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
        val unencryptedData = AesIgeCipher(
            CipherMode.DECRYPT,
            aesKeyIvPair
        ).finalize(encryptedData)

        val unencryptedStream = TLBufferedInputStream.wrap(unencryptedData)

        // Decompose
        val serverSalt = unencryptedStream.readLong()
        val sessionId = unencryptedStream.readLong()

        val messageBytes = unencryptedStream.readBytes(encryptedDataLength - 16)

        // Payload starts here
        val paddingSize = encryptedDataLength - 16 - messageBytes.size // serverSalt(8) + sessionId(8) + messageId(8) + seqNo(4) + msgLen(4)

        // Security checks
        if (paddingSize > 15 || paddingSize < 0) {
            error("Padding must be between 0 and 15 included, found $paddingSize")
        }

        if (session.id != sessionId) {
            error("The message was not intended for this session, expected ${session.id}, found $sessionId")
        }

/*
        // Check that msgKey is equal to the 128 lower-order bits of the SHA1 hash of the previously encrypted portion
        val checkMsgKey = generateMsgKey(serverSalt, sessionId, messageBytes)
        if (!Arrays.equals(checkMsgKey, msgKey))
            throw SecurityException("The message msgKey is inconsistent with it's data")
*/

        authCredentials.serverSalt = serverSalt
        return messageBytes
    }

    private suspend fun requireAuthCredentials() = mutex.withLock {
        requireNotNull(authCredentials) {
            "Authentication is required to unwrap data"
        }
    }

    private fun generateMsgKey(
        serverSalt: ByteArray,
        sessionId: Long,
        message: ByteArray
    ): ByteArray {
        val crypt = Digest(DigestMode.SHA1)

        crypt.updateData(
            serverSalt,
            longToBytes(sessionId),
            message
        )

        return subArray(crypt.digest(), 4, 16)
    }

    fun longToBytes(value: Long): ByteArray {
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
        println("CONNECTION: newNonce ${newNonce.bytes.toHex()}")

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

        println("CONNECTION: got serverDhInner")

        // Step 5
        val authKey = generateAuthKey(serverDhInner)

        println("CONNECTION: got authKey")

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
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val message = request.toPublicMessage(convertTimeToMessageId(currentTime))
        println("CONNECTION: auth sending $request")
        sendData(message)
        val rawResponse = listenToData().first()
        val parsedResponse = request.parseResponse(rawResponse)
        println("CONNECTION: auth received $parsedResponse")

        return parsedResponse
    }

    private suspend fun IConnection.sendReqPQ(): TLResPQ {
        val nonceBytes = secureRandom.getRandomBytes(16)

        val nonce = TLInt128(nonceBytes)
        val request = com.attafitamim.mtproto.client.scheme.methods.global.TLReqPq(nonce)

        return sendRequest(request)
    }

    private fun generateNewNonce(): TLInt256 {
        val newNonceBytes = secureRandom.getRandomBytes(32)
        return TLInt256(newNonceBytes)
    }

    private suspend fun IConnection.sendReqDhParams(
        resPq: TLResPQ.ResPQ,
        newNonce: TLInt256
    ): TLServerDHParams {
        val serverFingerPrints = resPq.serverPublicKeyFingerprints.toList()
        val rsaKey = serverKeys.firstOrNull { serverKey ->
            serverFingerPrints.contains(serverKey.fingerprint)
        } ?: error("No finger prints from the list are supported by the client: $serverFingerPrints")

        println("CONNECTION: rsaKey $rsaKey")

        val solvedPQ = PQSolver.solve(BigInteger.fromByteArray(resPq.pq, Sign.POSITIVE))

        println("CONNECTION: solvedPQ $solvedPQ")

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

        val pqDataHash = Digest(DigestMode.SHA1)
            .digest(pqDataBytes)

        val paddingSize = 255 - (pqDataBytes.size + pqDataHash.size)
        val padding = if (paddingSize > 0) Random.nextBytes(paddingSize) else ByteArray(0)
        val dataWithHash = pqDataHash + pqDataBytes + padding

        println("CONNECTION: beginning to do RSA, data with hash: ${dataWithHash.toHex()}")

        val encryptedData = RsaEcbCipher(
            CipherMode.ENCRYPT,
            rsaKey
        ).finalize(dataWithHash)

        println("CONNECTION: end RSA, encrypted data: ${encryptedData.toHex()}")

        val request = TLReqDHParams(
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
        newNonce: TLInt256,
        serverNonce: TLInt128
    ): AesKey {
        val key = Digest(
            DigestMode.SHA1
        ).digest(
            newNonce.bytes,
            serverNonce.bytes
        ) + Digest(
            DigestMode.SHA1
        ).digest(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(0..<12)


        val iv = Digest(
            DigestMode.SHA1
        ).digest(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(12..<20) + Digest(
            DigestMode.SHA1
        ).digest(
            newNonce.bytes,
            newNonce.bytes
        ) + newNonce.bytes.sliceArray(0..<4)

        return AesKey(key, iv)
    }

    private fun TLServerDHParams.ServerDHParamsOk.getDecryptedData(
        aesKey: AesKey
    ): TLServerDHInnerData {
        val answer = AesIgeCipher(
            CipherMode.DECRYPT,
            aesKey
        ).finalize(encryptedAnswer)

        val stream = TLBufferedInputStream.wrap(answer)
        val answerHash = stream.readBytes(20) // Hash
        val dhInner = TLServerDHInnerData.parse(stream)

        val serializedDhInner = serializeData {
            dhInner.serialize(this)
        }

        val serializedHash = Digest(
            DigestMode.SHA1
        ).digest(
            serializedDhInner
        )

        if (!answerHash.contentEquals(serializedHash)) {
            error("Security issue")
        }

        return dhInner
    }

    private fun generateAuthKey(
        serverDHInnerData: TLServerDHInnerData.ServerDHInnerData,
        size: Int = 256
    ): AuthKey {
        val b = loadBigInt(secureRandom.getRandomBytes(256))
        println("CONNECTION: got b $b")
        val g = BigInteger(serverDHInnerData.g)
        println("CONNECTION: got g $g")
        val dhPrime = loadBigInt(serverDHInnerData.dhPrime)
        println("CONNECTION: got dhPrime $dhPrime")
        val gb = g.modPow(b, dhPrime)
        println("CONNECTION: got gb $gb")

        val authKeyVal = loadBigInt(serverDHInnerData.gA).modPow(b, dhPrime)
        println("CONNECTION: got authKeyVal $authKeyVal")
        val authKey =  alignKeyZero(fromBigInt(authKeyVal), size)
        val keyId = subArray(Digest(DigestMode.SHA1).digest(authKey), 12, 8)
        println("CONNECTION: got keyId")

        val gbBytes = fromBigInt(gb)
        println("CONNECTION: got gbBytes")
        return AuthKey(authKey, keyId, gbBytes)
    }

    /* Iterative Function to calculate (x^y) in O(log y) */
    fun BigInteger.modPow(exponent: BigInteger, modulus: BigInteger): BigInteger {
        var x = this
        var y = exponent
        var res = BigInteger.ONE // Initialize result
        x %= modulus // Update x if it is more than or
        // equal to p
        if (x == BigInteger.ZERO) {
            return BigInteger.ZERO
        } // In case x is divisible by p;
        while (y > BigInteger.ZERO) {

            // If y is odd, multiply x with result
            if (y and BigInteger.ONE != BigInteger.ZERO) {
                res = res * x % modulus
            }

            // y must be even now
            y = y shr 1 // y = y/2
            x = x * x % modulus
        }
        return res
    }
    private fun fromBigInt(value: BigInteger): ByteArray {
        val res = value.toByteArray()
        return if (res[0].toInt() == 0) {
            val res2 = ByteArray(res.size - 1)
            res.copyInto(res2, 0, 1, res2.size)
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

    fun loadBigInt(data: ByteArray): BigInteger {
        return BigInteger.fromByteArray(data, Sign.POSITIVE)
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

        val innerDataWithHash = align(Digest(DigestMode.SHA1).digest(innerDataBytes) + innerDataBytes, 16)
        val dataWithHashEnc = AesIgeCipher(
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
        newNonce: TLInt256,
        authKey: AuthKey
    ): AuthCredentials? {
        val authAuxHash = Digest(DigestMode.SHA1).digest(authKey.key).sliceArray(0 ..< 8)

        return when (this) {
            is TLSetClientDHParamsAnswer.DhGenOk -> {
                val newNonceHash = subArray(Digest(
                    DigestMode.SHA1
                ).digest(
                    newNonce.bytes,
                    byteArrayOf(1),
                    authAuxHash
                ),4, 16)

                if (!newNonceHash1.bytes.contentEquals(newNonceHash)) {
                    error("Security issue")
                }

                val serverSalt = readLong(xor(subArray(newNonce.bytes, 0, 8), subArray(resPq.serverNonce.bytes, 0, 8)), 0)

                val authCredentials = AuthCredentials(authKey, authKey.id, serverSalt)

                println("AUTH_SUCCESS: $authCredentials")

                authCredentials
            }

            is TLSetClientDHParamsAnswer.DhGenRetry -> {
                val newNonceHash = subArray(Digest(
                    DigestMode.SHA1
                ).digest(
                    newNonce.bytes,
                    byteArrayOf(2),
                    authAuxHash
                ), 4, 16)

                if (!newNonceHash2.bytes.contentEquals(newNonceHash)) {
                    error("Security issue")
                }

                null
            }

            is TLSetClientDHParamsAnswer.DhGenFail -> {
                val newNonceHash = subArray(Digest(
                    DigestMode.SHA1
                ).digest(
                    newNonce.bytes,
                    byteArrayOf(3),
                    authAuxHash
                ), 4, 16)

                if (!newNonceHash3.bytes.contentEquals(newNonceHash)) {
                    error("Security issue")
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

    fun generateMsgKey(unencryptedData: ByteArray) = subArray(Digest(DigestMode.SHA1
    ).digest(unencryptedData), 4, 16)

    fun computeAesKey(
        authKey: ByteArray,
        msgKey: ByteArray,
        isOutgoing: Boolean = true
    ): AesKey {
        val x = if (isOutgoing) 0 else 8
        val a = Digest(DigestMode.SHA256).digest(msgKey, subArray(authKey, x, 36))
        val b = Digest(DigestMode.SHA256).digest(subArray(authKey, x + 40, 36), msgKey)

        val key = subArray(a, 0, 8) + subArray(b, 8, 16) + subArray(a, 24, 8)
        val iv = subArray(b, 0, 8) + subArray(a, 8, 16) + subArray(b, 24, 8)

        return AesKey(
            key,
            iv
        )
    }
}