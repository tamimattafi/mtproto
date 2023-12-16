package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.connection.auth.IAuthenticator
import com.attafitamim.mtproto.client.connection.core.IConnectionProvider
import com.attafitamim.mtproto.client.connection.exceptions.TLRequestError
import com.attafitamim.mtproto.client.connection.utils.createMessageId
import com.attafitamim.mtproto.client.connection.utils.generateSeqNo
import com.attafitamim.mtproto.client.scheme.containers.global.TLPublicMessage
import com.attafitamim.mtproto.client.scheme.methods.global.TLInitConnection
import com.attafitamim.mtproto.client.scheme.methods.global.TLInvokeWithLayer
import com.attafitamim.mtproto.client.scheme.methods.global.TLInvokeWithoutUpdates
import com.attafitamim.mtproto.client.scheme.types.global.TLBadMsgNotification
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgDetailedInfo
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgResendReq
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgsAck
import com.attafitamim.mtproto.client.scheme.types.global.TLNewSession
import com.attafitamim.mtproto.client.scheme.types.global.TLProtocolMessage
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcError
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcResult
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.serialization.utils.parseBytes
import com.attafitamim.mtproto.serialization.utils.serializeToBytes
import com.attafitamim.mtproto.serialization.utils.toTLInputStream
import com.attafitamim.mtproto.serialization.utils.tryParse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultConnectionManager(
    private val scope: CoroutineScope,
    private val connectionProvider: IConnectionProvider,
    private val authenticator: IAuthenticator,
    private val unknownMessageHandler: IUnknownMessageHandler?,
    private val passport: ConnectionPassport
) : IConnectionManager {

    private val mutex = Mutex()
    private val connectionSessions = mutableMapOf<ConnectionType, ConnectionSession>()

    private val messagesFlow = MutableSharedFlow<TLPublicMessage>()
    private val protocolMessagesFlow = MutableSharedFlow<TLProtocolMessage>()

    private val protocolParsersList: List<(TLInputStream) -> TLProtocolMessage> = listOf(
        TLRpcResult::parse,
        TLMsgsAck::parse,
        TLNewSession::parse,
        TLBadMsgNotification::parse,
        TLMsgResendReq::parse,
        TLMsgDetailedInfo::parse
    )

    init {
        handleProtocolMessages()
    }

    override suspend fun initConnection(connectionType: ConnectionType) {
        requireConnection(connectionType)
    }

    override suspend fun <T : Any> sendRequest(
        method: TLMethod<T>,
        connectionType: ConnectionType
    ): T {
        val connectionSession = requireConnection(connectionType)
        val messageId = connectionSession.sendRequest(method)
        return connectionSession.getResponse(method, messageId)
    }

    override suspend fun release() {
        TODO("Not yet implemented")
    }

    private suspend fun requireConnection(connectionType: ConnectionType) =
        connectionSessions[connectionType] ?: createConnection(connectionType)

    private suspend fun createConnection(connectionType: ConnectionType): ConnectionSession =
        mutex.withLock{
            val connection = connectionProvider.provideConnection()
            connection.connect()

            val session = authenticator.authenticate(connectionType, connection)

            val connectionSession = ConnectionSession(
                session,
                connection,
                connectionType
            )

            connectionSessions[connectionType] = connectionSession
            connectionSession.listenToMessages()

            return connectionSession
        }

    private suspend fun <T : Any> ConnectionSession.getResponse(
        method: TLMethod<T>,
        messageId: Long
    ): T = protocolMessagesFlow
        .asSharedFlow()
        .filterIsInstance(TLRpcResult::class)
        .mapNotNull { rpcResult ->
            getResponse(rpcResult, method, messageId)
        }.first()

    private fun <T : Any> ConnectionSession.getResponse(
        rpcResult: TLRpcResult,
        method: TLMethod<T>,
        messageId: Long
    ): T? = when (rpcResult) {
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

    private suspend fun ConnectionSession.sendRequest(method: TLMethod<*>): Long = mutex.withLock {
        val request = if (isInitialized) {
            method
        } else {
            val needsUpdates = connectionType is ConnectionType.Generic
            method.asInitQuery(needsUpdates)
        }

        isInitialized = true
        return sendMessage(request)
    }

    private fun <T : Any> TLMethod<T>.asInitQuery(needsUpdates: Boolean): TLMethod<*> {
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

    private suspend fun ConnectionSession.sendMessage(message: TLSerializable): Long {
        println("CONNECTION: sending api request $message")

        val messageBytes = message.serializeToBytes()
        val messageId = session.createMessageId()

        val requestMessage = TLPublicMessage(
            messageId,
            session.generateSeqNo(contentRelated = false),
            messageBytes.size,
            messageBytes
        )

        val serializedMessage = requestMessage.serializeToBytes()
        val firstMessageEncrypted = authenticator.wrapData(
            session,
            serializedMessage
        )

        connection.sendData(firstMessageEncrypted)

        return messageId
    }

    private suspend fun ConnectionSession.listenToMessages() = scope.launch {
        connection.listenToData().collect { rawResponse ->
            val data = authenticator.unwrapData(session, rawResponse)
            val message = data.toTLInputStream().tryParse(TLPublicMessage::parse)

            if (message != null) {
                acknowledgeMessage(message.msgId)
                messagesFlow.emit(message)
            } else {
                unknownMessageHandler?.handle(data)
            }
        }
    }

    private fun handleProtocolMessages() = scope.launch {
        messagesFlow.asSharedFlow().collect { message ->
            val protocolMessage = message.parseProtocolMessage()
            if (protocolMessage != null) {
                protocolMessagesFlow.emit(protocolMessage)
            } else {
                unknownMessageHandler?.handle(message.data)
            }
        }
    }

    private fun TLPublicMessage.parseProtocolMessage(): TLProtocolMessage? {
        val stream = data.toTLInputStream()
        protocolParsersList.forEach { parser ->
            val message = stream.tryParse(parser)
            if (message != null) {
                println("CONNECTION: protocol message $message")
                return message
            }
        }

        return null
    }

    private fun ConnectionSession.acknowledgeMessage(messageId: Long) = scope.launch {
        val messageIds = TLVector.Vector(listOf(messageId))
        val request = TLMsgsAck.MsgsAck(messageIds)
        sendMessage(request)
    }
}
