package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionState
import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.api.connection.IConnectionProvider
import com.attafitamim.mtproto.client.connection.auth.AuthCredentials
import com.attafitamim.mtproto.client.connection.auth.AuthKey
import com.attafitamim.mtproto.client.connection.auth.AuthResult
import com.attafitamim.mtproto.client.connection.auth.CryptoUtils
import com.attafitamim.mtproto.client.connection.auth.IAuthenticationStorage
import com.attafitamim.mtproto.client.connection.auth.PQLongSolver
import com.attafitamim.mtproto.client.connection.exceptions.TLConnectionError
import com.attafitamim.mtproto.client.connection.exceptions.TLRequestError
import com.attafitamim.mtproto.client.connection.interceptor.IRequestInterceptor
import com.attafitamim.mtproto.client.connection.session.Session
import com.attafitamim.mtproto.client.connection.utils.SECOND_IN_MILLIS
import com.attafitamim.mtproto.client.connection.utils.createMessageId
import com.attafitamim.mtproto.client.connection.utils.generateSeqNo
import com.attafitamim.mtproto.client.connection.utils.parseResponse
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.scheme.containers.global.TLEncryptedMessage
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt256
import com.attafitamim.mtproto.client.scheme.containers.global.TLPublicMessage
import com.attafitamim.mtproto.client.scheme.methods.global.TLInitConnection
import com.attafitamim.mtproto.client.scheme.methods.global.TLInvokeWithLayer
import com.attafitamim.mtproto.client.scheme.methods.global.TLInvokeWithoutUpdates
import com.attafitamim.mtproto.client.scheme.methods.global.TLReqDHParams
import com.attafitamim.mtproto.client.scheme.methods.global.TLReqPq
import com.attafitamim.mtproto.client.scheme.types.global.TLBadMsgNotification
import com.attafitamim.mtproto.client.scheme.types.global.TLClientDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLFutureSalts
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgDetailedInfo
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgResendReq
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgsAck
import com.attafitamim.mtproto.client.scheme.types.global.TLNewSession
import com.attafitamim.mtproto.client.scheme.types.global.TLPQInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLProtocolMessage
import com.attafitamim.mtproto.client.scheme.types.global.TLResPQ
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcError
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcResult
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHInnerData
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHParams
import com.attafitamim.mtproto.client.scheme.types.global.TLSetClientDHParamsAnswer
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.security.cipher.aes.AesIgeCipher
import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.aes.EncodedAesSecretKey
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.rsa.RsaEcbCipher
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.attafitamim.mtproto.security.digest.core.Digest
import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.obfuscation.DefaultObfuscator
import com.attafitamim.mtproto.security.utils.SecureRandom
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.utils.calculateData
import com.attafitamim.mtproto.serialization.utils.parseBytes
import com.attafitamim.mtproto.serialization.utils.serializeData
import com.attafitamim.mtproto.serialization.utils.serializeToBytes
import com.attafitamim.mtproto.serialization.utils.toTLInputStream
import com.attafitamim.mtproto.serialization.utils.tryParse
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import io.ktor.util.collections.ConcurrentMap
import kotlin.concurrent.Volatile
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

@OptIn(ExperimentalStdlibApi::class)
class ConnectionManager(
    private val connectionProvider: IConnectionProvider,
    private val authStorage: IAuthenticationStorage,
    private val serverKeys: List<RsaKey>,
    private val passport: ConnectionPassport,
    private val delegate: IConnectionDelegate? = null,
    private val interceptors: List<IRequestInterceptor> = emptyList()
) : IConnectionManager {

    private val localScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val globalMutex = Mutex()
    private val connectionSessions = ConcurrentMap<ConnectionType, ConnectionSession>()

    private val messagesFlow = MutableSharedFlow<TLPublicMessage>()
    private val protocolMessagesFlow = MutableSharedFlow<TLProtocolMessage>()

    private val requestsQueue = ConcurrentMap<ConnectionType, ArrayDeque<ByteArray>>()

    @Volatile
    private var authCredentials: AuthCredentials? = null
    private val secureRandom = SecureRandom()

    private val protocolParsersList: List<(TLInputStream) -> TLProtocolMessage> = listOf(
        TLRpcResult::parse,
        TLMsgsAck::parse,
        TLNewSession::parse,
        TLBadMsgNotification::parse,
        TLMsgResendReq::parse,
        TLMsgDetailedInfo::parse,
        TLFutureSalts::parse
    )

    init {
        handleProtocolMessages()
    }

    override suspend fun initConnection(connectionType: ConnectionType) {
        val initAsync = connectionScope.async {
            val newConnection = requireConnection(connectionType)
            newConnection.processQueue()
        }

        initAsync.await()
    }

    override suspend fun <R : Any> sendRequest(
        method: TLMethod<R>,
        connectionType: ConnectionType
    ): R {
        val connectionSession = requireConnection(connectionType)
        return connectionSession.connectionMutex.withLock {
            val messageId = createMessageId()
            if (interceptors.isEmpty()) {
                connectionSession.execute(messageId, method)
            } else {
                connectionSession.intercept(messageId, method)
            }
        }
    }

    override suspend fun disconnect() = globalMutex.withLock {
        forceDisconnect()
    }

    private suspend fun forceDisconnect() {
        connectionScope.coroutineContext.cancelChildren()

        connectionSessions.forEach { entry ->
            entry.value.obfuscatedConnection.connection.disconnect()
        }

        connectionSessions.clear()
    }

    override suspend fun release(resetAuth: Boolean) = globalMutex.withLock {
        forceDisconnect()
        localScope.coroutineContext.cancelChildren()

        if (resetAuth) {
            cleanup()
        }
    }

    private suspend fun <R : Any> ConnectionSession.intercept(
        messageId: Long,
        method: TLMethod<R>
    ): R {
        val position = 0
        val interceptor = interceptors[position]
        val chain = createInterceptorChain(
            position,
            messageId,
            method,
            connectionSession = this
        )

        return interceptor.intercept(chain)
    }

    private fun <R : Any> createInterceptorChain(
        position: Int,
        messageId: Long,
        method: TLMethod<R>,
        connectionSession: ConnectionSession
    ): IRequestInterceptor.Chain<R> = object : IRequestInterceptor.Chain<R>(
        position,
        messageId,
        method,
        connectionSession
    ) {
        override suspend fun proceed(newMethod: TLMethod<R>): R {
            val nextPosition = position + 1
            if (nextPosition > interceptors.lastIndex) {
                return connectionSession.execute(messageId, newMethod)
            }

            val interceptor = interceptors[nextPosition]
            val newChain = createInterceptorChain(
                nextPosition,
                messageId,
                newMethod,
                connectionSession
            )

            return interceptor.intercept(newChain)
        }
    }

    private suspend fun <R : Any> ConnectionSession.execute(
        messageId: Long,
        method: TLMethod<R>
    ): R {
        sendRequest(messageId, method)
        return getResponse(method, messageId)
    }

    private suspend fun requireConnection(connectionType: ConnectionType): ConnectionSession = globalMutex.withLock {
        val currentConnection = connectionSessions[connectionType]
        if (currentConnection != null) {
            return@withLock currentConnection
        }

        val connection = connectionProvider.provideConnection()
        while (!connection.connect()) {
            println("CONNECTION: connection failed")
            delay(1000L)
            println("CONNECTION: reconnecting")
        }

        val obfuscator = DefaultObfuscator()
        val initData = obfuscator.init()
        val sendDataSuccess = connection.sendData(initData)
        println("CONNECTION: type($connectionType) send init data success($sendDataSuccess)")

        val obfuscatedConnection = ObfuscatedConnection(connection, obfuscator)
        val session = authenticate(connectionType, obfuscatedConnection)
        println("CONNECTION: type($connectionType) created session(${session.uId})")

        val connectionSession = ConnectionSession(
            session,
            obfuscatedConnection,
            connectionType
        )

        connectionSessions[connectionType] = connectionSession
        connectionSession.listenToMessages()
        connectionSession.listenToDisconnect()

        delegate?.onSessionConnected(session.id, connectionType)
        return connectionSession
    }

    private suspend fun <R : Any> ConnectionSession.getResponse(
        method: TLMethod<R>,
        messageId: Long
    ): R = protocolMessagesFlow
        .asSharedFlow()
        .filterIsInstance(TLRpcResult::class)
        .mapNotNull { rpcResult ->
            val response = getResponse(rpcResult, method, messageId)
            println("CONNECTION: session(${session.uId}) message(${messageId.toHexString()}) get response($response)")
            response
        }.first()

    private fun <R : Any> ConnectionSession.getResponse(
        rpcResult: TLRpcResult,
        method: TLMethod<R>,
        messageId: Long
    ): R? = when (rpcResult) {

        is TLRpcResult.RpcResult -> if (rpcResult.reqMsgId != messageId) {
            null
        } else {
            val inputStream = rpcResult.result.toTLInputStream()
            val error = inputStream.tryParse(TLRpcError::parse)

            if (error != null) {
                handleErrorResponse(
                    rpcResult,
                    method,
                    error
                )
            } else {
                method.parseBytes(rpcResult.result)
            }
        }
    }

    private fun ConnectionSession.handleErrorResponse(
        rpcResult: TLRpcResult.RpcResult,
        method: TLMethod<*>,
        error: TLRpcError
    ): Nothing = when (error) {
        is TLRpcError.RpcError -> throw TLRequestError(
            method,
            rpcResult.reqMsgId,
            session.authKeyId,
            session.id,
            error.errorCode,
            error.errorMessage
        )
    }

    private suspend fun ConnectionSession.sendRequest(
        messageId: Long,
        method: TLMethod<*>
    ) {
        val request = if (isInitialized) {
            method
        } else {
            val needsUpdates = connectionType is ConnectionType.Generic
            method.asInitQuery(needsUpdates)
        }

        isInitialized = true
        sendMessage(messageId, request)
    }

    private fun <R : Any> TLMethod<R>.asInitQuery(needsUpdates: Boolean): TLMethod<*> {
        val initConnection = passport.run {
            TLInitConnection(
                apiId,
                apiHash,
                deviceModel,
                systemVersion,
                appVersion,
                systemLangCode,
                langPack,
                langCode,
                proxy = null,
                query = this@asInitQuery,
                parseX = ::parse
            )
        }

        val layerRequest = TLInvokeWithLayer(
            passport.layer,
            initConnection,
            initConnection::parse
        )

        return if (!needsUpdates) {
            TLInvokeWithoutUpdates(
                layerRequest,
                layerRequest::parse
            )
        } else {
            layerRequest
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun ConnectionSession.sendMessage(
        messageId: Long,
        message: TLSerializable
    ) {
        println("CONNECTION: session(${session.uId}) message(${messageId.toHexString()}) type($connectionType) sendMessage($message)")
        val messageBytes = message.serializeToBytes()

        val requestMessage = TLPublicMessage(
            messageId,
            session.generateSeqNo(contentRelated = false),
            messageBytes.size,
            messageBytes
        )

        val serializedMessage = requestMessage.serializeToBytes()
        val encryptedMessage = wrapData(
            session,
            serializedMessage
        )

        enqueueMessage(messageId, encryptedMessage)
    }

    private suspend fun ConnectionSession.enqueueMessage(
        messageId: Long,
        message: ByteArray
    ) {
        println("CONNECTION: session(${session.uId}) message(${messageId.toHexString()}) enqueue message(${message.toHex()})")
        val queue = requestsQueue.provideQueue(connectionType)
        queue.addLast(message)
        println("CONNECTION: session(${session.uId}) new queue size(${queue.size})")
        fireQueue()
    }

    private suspend fun ConnectionSession.processQueue() = connectionMutex.withLock {
        println("CONNECTION: session(${session.uId}) processQueue")
        fireQueue()
    }

    private suspend fun ConnectionSession.fireQueue() {
        println("CONNECTION: session(${session.uId}) fireQueue")
        val queue = requestsQueue.provideQueue(connectionType)
        while (queue.isNotEmpty()) {
            val message = queue.first()
            if (obfuscatedConnection.sendObfuscatedBytes(message)) {
                queue.remove(message)
            } else {
                println("session(${session.uId}) error sending obfuscated bytes [${message.toHex()}]")
                reconnect()
                break
            }
        }

        println("CONNECTION: session(${session.uId}) fireQueue END with queue size(${queue.size})")
    }

    private suspend fun reconnect(connectionType: ConnectionType) {
        println("CONNECTION: type($connectionType) reconnecting")
        removeConnection(connectionType)
        initConnection(connectionType)
    }

    private suspend fun removeConnection(connectionType: ConnectionType) = globalMutex.withLock {
        connectionSessions.remove(connectionType)?.apply {
            obfuscatedConnection.apply {
                connection.disconnect()
                obfuscator.release()
            }
        }
    }

    private suspend fun ObfuscatedConnection.sendObfuscatedBytes(byteArray: ByteArray): Boolean =
        try {
            val packetBytes = serializeData {
                writeInt(byteArray.size)
                writeByteArray(byteArray)
            }

            val obfuscatedBytes = obfuscator.obfuscate(packetBytes)
            connection.sendData(obfuscatedBytes)
        } catch (exception: Exception) {
            println("CONNECTION: Error sending obfuscated data $exception")
            false
        }

    private fun ObfuscatedConnection.listenToData() = connection.listenToData().map { data ->
        val clarifiedBytes = obfuscator.clarify(data)
        val inputStream = clarifiedBytes.toTLInputStream()
        val size = inputStream.readInt()

        inputStream.readBytes(size)
    }

    private fun ConnectionSession.listenToMessages() = connectionScope.launch {
        obfuscatedConnection.listenToData().collect { rawResponse ->
            val response = unwrapData(session, rawResponse)
            val message = response.toTLInputStream().tryParse(TLPublicMessage::parse)

            if (message != null) {
                acknowledgeMessage(message.msgId)
                messagesFlow.emit(message)
            } else {
                delegate?.onUnknownMessage(response)
            }
        }
    }

    private fun ConnectionSession.listenToDisconnect() = connectionScope.launch {
        obfuscatedConnection.connection.listenToState()
            .collect { state ->
                println("CONNECTION: session(${session.uId}) type($connectionType) new state($state)")
                if (state == ConnectionState.Disconnected) {
                    connectionMutex.withLock {
                        reconnect()
                    }
                }
            }
    }

    private suspend fun ConnectionSession.reconnect() {
        if (isReconnecting) return
        isReconnecting = true

        reconnect(connectionType)
    }

    private fun handleProtocolMessages() = localScope.launch {
        messagesFlow.asSharedFlow().collect { message ->
            val protocolMessage = message.parseProtocolMessage()
            if (protocolMessage != null) {
                println("CONNECTION: protocolMessage($protocolMessage)")
                protocolMessagesFlow.emit(protocolMessage)
            } else {
                delegate?.onUnknownMessage(message.data)
            }
        }
    }

    private fun TLPublicMessage.parseProtocolMessage(): TLProtocolMessage? {
        val stream = data.toTLInputStream()
        protocolParsersList.forEach { parser ->
            val message = stream.tryParse(parser)
            if (message != null) {
                return message
            }
        }

        return null
    }

    private fun ConnectionSession.acknowledgeMessage(messageId: Long) = connectionScope.launch {
        val messageIds = TLVector.Vector(listOf(messageId))
        val request = TLMsgsAck.MsgsAck(messageIds)

        connectionMutex.withLock {
            val acknowledgmentMessageId = createMessageId()
            sendMessage(acknowledgmentMessageId, request)
        }
    }

    private suspend fun authenticate(
        connectionType: ConnectionType,
        connection: ObfuscatedConnection
    ): Session {
        val authCredentials = authenticate(connection)
        val currentSession = authStorage.getSession(connectionType)
        if (currentSession != null) {
            return currentSession
        }

        val id = secureRandom.getRandomLong()
        val authKeyId = authCredentials.keyId.toHex(appendSpaces = false)
        val newSession = Session(id, authKeyId)
        authStorage.saveSession(connectionType, newSession)
        return newSession
    }

    private suspend fun authenticate(connection: ObfuscatedConnection): AuthCredentials {
        val currentAuthCredentials = authCredentials
        if (currentAuthCredentials != null) {
            return currentAuthCredentials
        }

        val savedAuthCredentials = authStorage.getAuthCredentials()
        if (savedAuthCredentials != null) {
            authCredentials = savedAuthCredentials
            return savedAuthCredentials
        }

        val authResult = connection.generateAuthKey()
        authStorage.saveAuthCredentials(authResult.credentials)
        authCredentials = authResult.credentials

        return authResult.credentials
    }

    private fun cleanup() {
        authCredentials = null
        authStorage.clear()
    }

    private fun wrapData(session: Session, data: ByteArray): ByteArray {
        val authCredentials = requireAuthCredentials()

        val encryptedMessage = TLEncryptedMessage(
            authCredentials.serverSalt,
            session.id,
            data
        )

        val encryptedMessageBytes = encryptedMessage.serializeToBytes()
        val msgKey = generateMsgKey(encryptedMessageBytes)

        // Encrypt data
        val aesKey = computeAesKey(authCredentials.key.key, msgKey)
        val encryptedData = AesIgeCipher(
            CipherMode.ENCRYPT,
            aesKey,
        ).finalize(CryptoUtils.align(encryptedMessageBytes, 16))


        return serializeData(24 + encryptedData.size) {
            writeByteArray(authCredentials.keyId)
            writeByteArray(msgKey)
            writeByteArray(encryptedData)
        }
    }

    private fun unwrapData(session: Session, data: ByteArray): ByteArray {
        val authCredentials = requireAuthCredentials()

        val stream = TLBufferedInputStream.wrap(data)

        // Retrieve and check authKey
        val size = data.size
        val msgAuthKeyId = stream.readBytes(8)
        if (!authCredentials.keyId.contentEquals(msgAuthKeyId))
            connectionError("Message's authKey ${authCredentials.keyId.toHex()} doesn't match given authKey ${msgAuthKeyId.toHex()}")

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
            connectionError("Padding must be between 0 and 15 included, found $paddingSize")
        }

        if (session.id != sessionId) {
            connectionError("The message was not intended for this session, expected ${session.uId}, found ${sessionId.toULong()}")
        }

        // TODO: important part, handle it
        /*
                // Check that msgKey is equal to the 128 lower-order bits of the SHA1 hash of the previously encrypted portion
                val checkMsgKey = generateMsgKey(serverSalt, sessionId, messageBytes)
                if (!Arrays.equals(checkMsgKey, msgKey))
                    throw SecurityException("The message msgKey is inconsistent with it's data")
        */

        authCredentials.serverSalt = serverSalt
        return messageBytes
    }

    private fun requireAuthCredentials() =
        requireNotNull(authCredentials) {
            "Authentication is required to unwrap data"
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

        return CryptoUtils.subArray(crypt.digest(), 4, 16)
    }

    private fun longToBytes(value: Long): ByteArray {
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

    private suspend fun ObfuscatedConnection.generateAuthKey(): AuthResult {
        // Step 0
        val resPq = sendReqPQ()
        if (resPq !is TLResPQ.ResPQ) {
            connectionError("Unknown TLResPQ variant: $resPq")
        }

        // Step 1
        val newNonce = generateNewNonce()

        // Step 2
        val serverDhParams = sendReqDhParams(resPq, newNonce)
        if (serverDhParams !is TLServerDHParams.ServerDHParamsOk) {
            connectionError("Unknown TLServerDHParams variant: $serverDhParams")
        }

        // Step 3
        val tempAesCredentials = generateAesKey(
            newNonce,
            resPq.serverNonce
        )

        // Step 4
        val serverDhInner = serverDhParams.getDecryptedData(tempAesCredentials)
        if (serverDhInner !is TLServerDHInnerData.ServerDHInnerData) {
            connectionError("Unknown TLServerDHInnerData variant: $serverDhInner")
        }

        // Step 5
        val authKey = generateAuthKey(serverDhInner)
        val retries = 5
        repeat(retries) { retryId ->
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

        connectionError("Connection failed after $retries tries")
    }

    private suspend fun <T : Any> ObfuscatedConnection.sendRequest(request: TLMethod<T>): T {
        val message = request.toPublicMessage()

        sendObfuscatedBytes(message)
        val rawResponse = listenToData().first()
        return request.parseResponse(rawResponse)
    }

    private suspend fun ObfuscatedConnection.sendReqPQ(): TLResPQ {
        val nonceBytes = secureRandom.getRandomBytes(16)

        val nonce = TLInt128(nonceBytes)
        val request = TLReqPq(nonce)

        return sendRequest(request)
    }

    private fun generateNewNonce(): TLInt256 {
        val newNonceBytes = secureRandom.getRandomBytes(32)
        return TLInt256(newNonceBytes)
    }

    private suspend fun ObfuscatedConnection.sendReqDhParams(
        resPq: TLResPQ.ResPQ,
        newNonce: TLInt256
    ): TLServerDHParams {
        val serverFingerPrints = resPq.serverPublicKeyFingerprints.toList()
        val rsaKey = serverKeys.firstOrNull { serverKey ->
            serverFingerPrints.contains(serverKey.fingerprint)
        } ?: connectionError("No fingerprints from the list are supported by the client: $serverFingerPrints")

        val pq = BigInteger.fromByteArray(resPq.pq, Sign.POSITIVE).longValue(exactRequired = true)

        println("CONNECTION: Start solve PQ $pq")
        val startAt = Clock.System.now().toEpochMilliseconds()
        val solvedPQ = PQLongSolver.solve(pq)
        val endAt = Clock.System.now().toEpochMilliseconds()
        val time = endAt - startAt
        println("CONNECTION: End solve PQ $pq in $time with p: ${solvedPQ.p} and q: ${solvedPQ.q}")
        val solvedP = longToBytes(solvedPQ.p)
        val solvedQ = longToBytes(solvedPQ.q)
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

        val encryptedData = RsaEcbCipher(
            CipherMode.ENCRYPT,
            rsaKey
        ).finalize(dataWithHash)

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

        val aesSecretKey = EncodedAesSecretKey(key)
        return AesKey(aesSecretKey, iv)
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
            connectionError("Security issue while decrypting Dh parameters data")
        }

        return dhInner
    }

    private fun generateAuthKey(
        serverDHInnerData: TLServerDHInnerData.ServerDHInnerData,
        size: Int = 256
    ): AuthKey {
        val b = loadBigInt(secureRandom.getRandomBytes(256))
        val g = BigInteger(serverDHInnerData.g)
        val dhPrime = loadBigInt(serverDHInnerData.dhPrime)
        val gb = g.modPow(b, dhPrime)

        val authKeyVal = loadBigInt(serverDHInnerData.gA).modPow(b, dhPrime)
        val authKey =  alignKeyZero(fromBigInt(authKeyVal), size)
        val keyId = CryptoUtils.subArray(Digest(DigestMode.SHA1).digest(authKey), 12, 8)

        val gbBytes = fromBigInt(gb)
        return AuthKey(authKey, keyId, gbBytes)
    }

    /* Iterative Function to calculate (x^y) in O(log y) */
    private fun BigInteger.modPow(exponent: BigInteger, modulus: BigInteger): BigInteger {
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

    private fun alignKeyZero(src: ByteArray, size: Int): ByteArray {
        if (src.size == size) {
            return src
        }
        return if (src.size > size) {
            CryptoUtils.subArray(
                src,
                src.size - size,
                size
            )
        } else {
            ByteArray(size - src.size) + src
        }
    }

    private fun loadBigInt(data: ByteArray): BigInteger {
        return BigInteger.fromByteArray(data, Sign.POSITIVE)
    }

    private suspend fun ObfuscatedConnection.sendReqSetDhClientParams(
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

        val innerDataWithHash =
            CryptoUtils.align(Digest(DigestMode.SHA1).digest(innerDataBytes) + innerDataBytes, 16)
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

    private fun readUInt(src: ByteArray, offset: Int): Long {
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
                val newNonceHash = CryptoUtils.subArray(
                    Digest(
                        DigestMode.SHA1
                    ).digest(
                        newNonce.bytes,
                        byteArrayOf(1),
                        authAuxHash
                    ), 4, 16
                )

                if (!newNonceHash1.bytes.contentEquals(newNonceHash)) {
                    connectionError("Security issue while processing DhGenOk")
                }

                val serverSalt = readLong(
                    CryptoUtils.xor(
                        CryptoUtils.subArray(
                            newNonce.bytes,
                            0,
                            8
                        ), CryptoUtils.subArray(resPq.serverNonce.bytes, 0, 8)
                    ), 0)
                AuthCredentials(authKey, authKey.id, serverSalt)
            }

            is TLSetClientDHParamsAnswer.DhGenRetry -> {
                val newNonceHash = CryptoUtils.subArray(
                    Digest(
                        DigestMode.SHA1
                    ).digest(
                        newNonce.bytes,
                        byteArrayOf(2),
                        authAuxHash
                    ), 4, 16
                )

                if (!newNonceHash2.bytes.contentEquals(newNonceHash)) {
                    connectionError("Security issue while processing DhGenRetry")
                }

                null
            }

            is TLSetClientDHParamsAnswer.DhGenFail -> {
                val newNonceHash = CryptoUtils.subArray(
                    Digest(
                        DigestMode.SHA1
                    ).digest(
                        newNonce.bytes,
                        byteArrayOf(3),
                        authAuxHash
                    ), 4, 16
                )

                if (!newNonceHash3.bytes.contentEquals(newNonceHash)) {
                    connectionError("Security issue while processing DhGenFail")
                }

                connectionError("Failed to generate Dh parameters")
            }
        }
    }

    private fun <R : Any> TLMethod<R>.toPublicMessage(): ByteArray {
        val authKeyId = 0L
        val messageId = createMessageId()
        val methodBytesSize = calculateData(::serialize)
        val methodBytes = serializeData(methodBytesSize, ::serialize)

        return serializeData {
            writeLong(authKeyId)
            writeLong(messageId)
            writeInt(methodBytesSize)
            writeByteArray(methodBytes)
        }
    }

    private fun <T : Any> TLVector<T>.toList() = when (this) {
        is TLVector.Vector -> elements
    }

    private fun generateMsgKey(unencryptedData: ByteArray) = CryptoUtils.subArray(
        Digest(
            DigestMode.SHA1
        ).digest(unencryptedData), 4, 16
    )

    private fun computeAesKey(
        authKey: ByteArray,
        msgKey: ByteArray,
        isOutgoing: Boolean = true
    ): AesKey {
        val x = if (isOutgoing) 0 else 8
        val a = Digest(DigestMode.SHA256).digest(msgKey, CryptoUtils.subArray(authKey, x, 36))
        val b = Digest(DigestMode.SHA256).digest(CryptoUtils.subArray(authKey, x + 40, 36), msgKey)

        val key = CryptoUtils.subArray(a, 0, 8) + CryptoUtils.subArray(
            b,
            8,
            16
        ) + CryptoUtils.subArray(a, 24, 8)
        val iv = CryptoUtils.subArray(b, 0, 8) + CryptoUtils.subArray(
            a,
            8,
            16
        ) + CryptoUtils.subArray(b, 24, 8)

        val aesSecretKey = EncodedAesSecretKey(key)
        return AesKey(
            aesSecretKey,
            iv
        )
    }

    private fun MutableMap<ConnectionType, ArrayDeque<ByteArray>>.provideQueue(key: ConnectionType) =
        getOrPut(key, ::ArrayDeque)

    private fun connectionError(message: String): Nothing {
        throw TLConnectionError(message)
    }
}
