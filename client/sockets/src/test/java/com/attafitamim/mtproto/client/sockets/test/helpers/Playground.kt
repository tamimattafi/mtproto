package com.attafitamim.mtproto.client.sockets.test.helpers

import com.attafitamim.mtproto.client.sockets.buffer.JavaByteBuffer
import com.attafitamim.mtproto.client.sockets.connection.IConnection
import com.attafitamim.mtproto.client.sockets.connection.Session
import com.attafitamim.mtproto.client.sockets.connection.SocketConnection
import com.attafitamim.mtproto.client.sockets.core.TimeManager
import com.attafitamim.mtproto.client.sockets.obfuscation.DefaultObfuscator
import com.attafitamim.mtproto.client.sockets.obfuscation.IObfuscator
import com.attafitamim.mtproto.client.sockets.obfuscation.ISecureRandom
import com.attafitamim.mtproto.client.sockets.obfuscation.cipher.JavaCipherFactory
import com.attafitamim.mtproto.client.sockets.obfuscation.toHex
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.AES256IGEDecrypt
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.AES256IGEEncrypt
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.align
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.alignKeyZero
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.loadBigInt
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.substring
import com.attafitamim.mtproto.client.sockets.secure.CryptoUtils.xor
import com.attafitamim.mtproto.client.sockets.secure.MTProtoMessageEncryption
import com.attafitamim.mtproto.client.sockets.serialization.PQSolver
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedInputStream
import com.attafitamim.mtproto.client.sockets.utils.calculateData
import com.attafitamim.mtproto.client.sockets.utils.serializeData
import com.attafitamim.scheme.mtproto.containers.global.TLInt128
import com.attafitamim.scheme.mtproto.containers.global.TLInt256
import com.attafitamim.scheme.mtproto.containers.global.TLMessage
import com.attafitamim.scheme.mtproto.methods.global.TLInitConnection
import com.attafitamim.scheme.mtproto.methods.global.TLInvokeWithLayer
import com.attafitamim.scheme.mtproto.methods.global.TLReqDHParams
import com.attafitamim.scheme.mtproto.methods.global.TLReqPq
import com.attafitamim.scheme.mtproto.methods.global.TLSetClientDHParams
import com.attafitamim.scheme.mtproto.methods.help.TLHelpGetServerConfig
import com.attafitamim.scheme.mtproto.types.global.TLClientDHInnerData
import com.attafitamim.scheme.mtproto.types.global.TLPQInnerData
import com.attafitamim.scheme.mtproto.types.global.TLResPQ
import com.attafitamim.scheme.mtproto.types.global.TLServerDHInnerData
import com.attafitamim.scheme.mtproto.types.global.TLServerDHParams
import com.attafitamim.scheme.mtproto.types.global.TLSetClientDHParamsAnswer
import com.attafitamim.scheme.mtproto.types.global.TLVector
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import kotlinx.coroutines.delay

object Playground {

    private const val WEB_SOCKET_URL = "wss://localhost:2047/ws"
    private const val SERVER_IP = "127.0.0.1"
    private const val SERVER_PORT = 2047

    suspend fun initConnection() {
        val endpointProvider = ConnectionHelper.createdEndpointProvider(SERVER_IP, SERVER_PORT)
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)

        val timeManager = TimeManager()
        val obfuscator = createObfuscator()
        val connection: IConnection = SocketConnection(
            timeManager,
            obfuscator,
            socketProvider
        )

        // Step 0
        connection.connect()
        delay(1000)

        // Step 1
        val authResult = connection.generateAuthKey()
        timeManager.setServerTime(authResult.serverTimeInSeconds * 1000L)

        // Step 2
        val session = Session(timeManager)

        // Step 3
        val getServerConfig = TLHelpGetServerConfig

        // Step 4
        val initConnection = TLInitConnection(
            apiId = 123,
            apiHash = "",
            deviceModel = "desktop",
            systemVersion = "1.2.3",
            appVersion = "playground-1",
            systemLangCode = "en",
            langPack = "en",
            langCode = "en",
            proxy = null,
            query = getServerConfig,
            parseX = getServerConfig::parse
        )

        // Step 5
        val initWithLayer = TLInvokeWithLayer(
            layer = 130,
            initConnection,
            initConnection::parse
        )

        // Step 6
        val requestSize = calculateData {
            initWithLayer.serialize(this)
        }

        val message = TLMessage(
            session.generateMessageId(),
            session.generateSeqNo(contentRelated = false),
            requestSize,
            initWithLayer
        )

        val encryptedMessage = MTProtoMessageEncryption.encrypt(
            authResult.credentials.key,
            authResult.credentials.keyId,
            session.id,
            authResult.credentials.serverSalt,
            message
        )

        val response = connection.sendRawRequest(encryptedMessage)
        println("RESPONSE_INIT_CONNECTION: ${response.toHex()}")
    }

    private suspend fun IConnection.generateAuthKey(): AuthResult {
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
        val tempAesCredentials = generateAesIgeCredentials(
            newNonce,
            resPq.serverNonce
        )

        // Step 4
        val serverDhInner = serverDhParams.getDecryptedData(tempAesCredentials)
        if (serverDhInner !is TLServerDHInnerData.ServerDHInnerData) {
            error("TLServerDHInnerData variant not supported $serverDhInner")
        }

        // Step 5
        val authKey = generateAuthKey(serverDhInner)

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
                    serverDhInner.serverTime
                )
            }
        }

        error("AUTH_FAILED")
    }

    private suspend fun IConnection.sendReqPQ(): TLResPQ {
        val nonceBytes = ByteArray(16)
        SecureRandom().nextBytes(nonceBytes)

        val nonce = TLInt128(nonceBytes)
        val request = TLReqPq(nonce)

        return sendRequest(request)
    }

    private fun generateNewNonce(): TLInt256 {
        val newNonceBytes = ByteArray(32)
        SecureRandom().nextBytes(newNonceBytes)
        return TLInt256(newNonceBytes)
    }

    private suspend fun IConnection.sendReqDhParams(
        resPq: TLResPQ.ResPQ,
        newNonce: TLInt256
    ): TLServerDHParams {


        val serverFingerPrints = resPq.serverPublicKeyFingerprints.toList()
        val fingerPrintSupported = serverFingerPrints.contains(fingerprint)
        if (!fingerPrintSupported) {
            error("No finger prints from the list are supported by the client: $serverFingerPrints")
        }

        val solvedPQ = PQSolver.solve(BigInteger(1, resPq.pq))

        val solvedP = fromBigInt(solvedPQ.p)
        val solvedQ = fromBigInt(solvedPQ.q)
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

        val pqDataHash = digestSha1(pqDataBytes)
        val paddingSize = 255 - (pqDataBytes.size + pqDataHash.size)
        val padding = if (paddingSize > 0) RandomUtils.randomByteArray(paddingSize) else ByteArray(0)
        val dataWithHash = pqDataHash + pqDataBytes + padding
        val encryptedData = rsa(publicKey, exponent, dataWithHash)

        val request = TLReqDHParams(
            resPq.nonce,
            resPq.serverNonce,
            solvedP,
            solvedQ,
            fingerprint,
            encryptedData
        )

        return sendRequest(request)
    }

    private fun generateAesIgeCredentials(
        newNonce: TLInt256,
        serverNonce: TLInt128
    ): AesIgeCredentials {
        val key = digestSha1(
            newNonce.bytes,
            serverNonce.bytes
        ) + digestSha1(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(0..<12)


        val iv = digestSha1(
            serverNonce.bytes,
            newNonce.bytes
        ).sliceArray(12..<20) + digestSha1(
            newNonce.bytes,
            newNonce.bytes
        ) + newNonce.bytes.sliceArray(0..<4)

        return AesIgeCredentials(key, iv)
    }

    private fun TLServerDHParams.ServerDHParamsOk.getDecryptedData(
        aesIgeCredentials: AesIgeCredentials
    ): TLServerDHInnerData {
        val answer = AES256IGEDecrypt(
            encryptedAnswer,
            aesIgeCredentials.iv,
            aesIgeCredentials.key
        )

        val stream = TLBufferedInputStream
            .Provider(JavaByteBuffer)
            .wrap(answer)

        val answerHash = stream.readBytes(20) // Hash
        val dhInner = TLServerDHInnerData.parse(stream)
        println("DH_INNER: $dhInner")

        val serializedDhInner = serializeData {
            dhInner.serialize(this)
        }

        val serializedHash = digestSha1(serializedDhInner)
        if (!answerHash.contentEquals(serializedHash)) {
            throw SecurityException()
        }

        println("DH_INNER: security passed")

        return dhInner
    }

    private fun generateAuthKey(
        serverDHInnerData: TLServerDHInnerData.ServerDHInnerData,
        size: Int = 256
    ): AuthKey {
        val b = loadBigInt(RandomUtils.randomByteArray(256))
        val g = BigInteger(serverDHInnerData.g.toString())
        val dhPrime = loadBigInt(serverDHInnerData.dhPrime)
        val gb = g.modPow(b, dhPrime)

        val authKeyVal = loadBigInt(serverDHInnerData.gA).modPow(b, dhPrime)
        val authKey =  alignKeyZero(fromBigInt(authKeyVal), size)
        val keyId = substring(digestSha1(authKey), 12, 8)

        val gbBytes = fromBigInt(gb)
        return AuthKey(authKey, keyId, gbBytes)
    }

    private suspend fun IConnection.sendReqSetDhClientParams(
        resPq: TLResPQ.ResPQ,
        aesIgeCredentials: AesIgeCredentials,
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

        val innerDataWithHash = align(digestSha1(innerDataBytes) + innerDataBytes, 16)
        val dataWithHashEnc = AES256IGEEncrypt(innerDataWithHash, aesIgeCredentials.iv, aesIgeCredentials.key)

        val request = TLSetClientDHParams(resPq.nonce, resPq.serverNonce, dataWithHashEnc)
        return sendRequest(request)
    }

    private fun TLSetClientDHParamsAnswer.toAuthCredentials(
        resPq: TLResPQ.ResPQ,
        newNonce: TLInt256,
        authKey: AuthKey
    ): AuthCredentials? {
        val authAuxHash = digestSha1(authKey.key).sliceArray(0 ..< 8)

        return when (this) {
            is TLSetClientDHParamsAnswer.DhGenOk -> {
                val newNonceHash = substring(digestSha1(
                    newNonce.bytes,
                    byteArrayOf(1),
                    authAuxHash
                ),4, 16)

                if (!newNonceHash1.bytes.contentEquals(newNonceHash)) {
                    throw SecurityException()
                }

                val serverSalt = readLong(xor(substring(newNonce.bytes, 0, 8), substring(resPq.serverNonce.bytes, 0, 8)), 0)

                val authCredentials = AuthCredentials(authKey.key, authKey.id, serverSalt)

                println("AUTH_SUCCESS: $authCredentials")

                authCredentials
            }

            is TLSetClientDHParamsAnswer.DhGenRetry -> {
                val newNonceHash = substring(digestSha1(
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
                val newNonceHash = substring(digestSha1(
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

    data class AesIgeCredentials(
        val key: ByteArray,
        val iv: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AesIgeCredentials

            if (!key.contentEquals(other.key)) return false
            if (!iv.contentEquals(other.iv)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }

    data class AuthKey(
        val key: ByteArray,
        val id: ByteArray,
        val gb: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AuthKey

            if (!key.contentEquals(other.key)) return false
            if (!id.contentEquals(other.id)) return false
            if (!gb.contentEquals(other.gb)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.contentHashCode()
            result = 31 * result + id.contentHashCode()
            result = 31 * result + gb.contentHashCode()
            return result
        }
    }

    data class AuthResult(
        val credentials: AuthCredentials,
        val serverTimeInSeconds: Int
    )

    data class AuthCredentials(
        val key: ByteArray,
        val keyId: ByteArray,
        val serverSalt: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AuthCredentials

            if (!key.contentEquals(other.key)) return false
            if (!keyId.contentEquals(other.keyId)) return false
            if (serverSalt != other.serverSalt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.contentHashCode()
            result = 31 * result + keyId.contentHashCode()
            result = 31 * result + serverSalt.hashCode()
            return result
        }
    }

    fun digestSha1(vararg src: ByteArray): ByteArray {
        val crypt: MessageDigest = MessageDigest.getInstance("SHA-1")

        src.forEach { source ->
            crypt.update(source)
        }

        return crypt.digest()
    }

    fun rsa(
        publicKey: String,
        exponent: String,
        src: ByteArray
    ): ByteArray {
        val keyFactory = KeyFactory.getInstance("RSA")
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        val keySpec = keyFactory.generatePublic(RSAPublicKeySpec(BigInteger(publicKey, 16), BigInteger(exponent, 16)))

        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        return cipher.doFinal(src)
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

    private fun <T : Any> TLVector<T>.toList() = when (this) {
        is TLVector.Vector -> elements
    }

    fun createSecureRandom(): ISecureRandom {
        val secureRandom = SecureRandom()

        return object : ISecureRandom {
            override fun getRandomBytes(size: Int): ByteArray {
                val bytes = ByteArray(size)
                secureRandom.nextBytes(bytes)

                return bytes
            }
        }
    }

    private fun createObfuscator(): IObfuscator {
        val secureRandom = createSecureRandom()
        val cipherFactory = JavaCipherFactory()

        return DefaultObfuscator(
            secureRandom,
            JavaByteBuffer.Companion,
            cipherFactory
        )
    }
}