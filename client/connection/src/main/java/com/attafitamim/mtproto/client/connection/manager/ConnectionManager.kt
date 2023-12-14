package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.connection.auth.IAuthenticator
import com.attafitamim.mtproto.client.connection.core.IConnectionProvider
import com.attafitamim.mtproto.client.connection.exceptions.RequestError
import com.attafitamim.mtproto.client.connection.utils.createMessageId
import com.attafitamim.mtproto.client.connection.utils.generateSeqNo
import com.attafitamim.mtproto.client.scheme.containers.global.TLPublicMessage
import com.attafitamim.mtproto.client.scheme.types.global.TLBadMsgNotification
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgDetailedInfo
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgResendReq
import com.attafitamim.mtproto.client.scheme.types.global.TLMsgsAck
import com.attafitamim.mtproto.client.scheme.types.global.TLNewSession
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcError
import com.attafitamim.mtproto.client.scheme.types.global.TLRpcResult
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import com.attafitamim.mtproto.serialization.utils.parseBytes
import com.attafitamim.mtproto.serialization.utils.serializeToBytes
import com.attafitamim.mtproto.serialization.utils.toInputStream
import com.attafitamim.mtproto.serialization.utils.tryParse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConnectionManager(
    private val scope: CoroutineScope,
    private val connectionProvider: IConnectionProvider,
    private val authenticator: IAuthenticator,
    private val passport: ConnectionPassport
) : IConnectionManager {

    private val mutex = Mutex()
    private val connectionSessions = mutableMapOf<ConnectionType, ConnectionSession>()

    private val unparsedMessagesFlow = MutableSharedFlow<ByteArray>()

    private var isInited = false

    override suspend fun <T : Any> sendRequest(
        method: TLMethod<T>,
        connectionType: ConnectionType
    ): T = mutex.withLock {
        val connectionSession = requireConnection(connectionType)
        val messageId = connectionSession.sendRequest(method)
        isInited = true
        return connectionSession.getResponse(method, messageId)
    }

    override suspend fun release() {
        TODO("Not yet implemented")
    }

    private suspend fun requireConnection(connectionType: ConnectionType) =
        connectionSessions[connectionType] ?: createConnection(connectionType)

    private suspend fun createConnection(connectionType: ConnectionType): ConnectionSession {
        val connection = connectionProvider.provideConnection()
        val session = authenticator.generateSession(connectionType)

        val connectionSession = ConnectionSession(
            session.id,
            session,
            connection,
            connectionType
        )

        connectionSessions[connectionType] = connectionSession

        connection.connect()
        authenticator.authenticate(connectionSession)

        return connectionSession
    }

    private suspend fun <T : Any> ConnectionSession.getResponse(
        method: TLMethod<T>,
        messageId: Long
    ): T = listenToMessage()
        .mapNotNull { message ->
            when (val response = message.parseProtocolData()) {
                is TLRpcResult.RpcResult -> {
                    if (response.reqMsgId != messageId) {
                        return@mapNotNull null
                    }

                    val inputStream = response.result.toInputStream()
                    val error = inputStream.tryParse(TLRpcError::parse)
                    if (error != null) {
                        error as TLRpcError.RpcError
                        throw RequestError(
                            error.errorCode,
                            error.errorMessage
                        )
                    }

                    method.parseBytes(response.result)
                }

                is TLRpcError.RpcError -> {
                    throw RequestError(
                        response.errorCode,
                        response.errorMessage
                    )
                }

                else -> {
                    null
                }
            }
        }.first()

    private fun TLPublicMessage.parseProtocolData(): TLObject? {
        val protocolParsers = listOf(
            TLRpcResult::parse,
            TLMsgsAck::parse,
            TLNewSession::parse,
            TLBadMsgNotification::parse,
            TLMsgResendReq::parse,
            TLMsgDetailedInfo::parse
        )

        val stream = data.toInputStream()
        protocolParsers.forEach { parser ->
            val data = stream.tryParse(parser)
            if (data != null) {
                println("PROTOCOL_DATA: $data")
                return data
            }
        }

        return null
    }

    private suspend fun ConnectionSession.sendRequest(method: TLMethod<*>): Long {
        val request = if (isInited) {
            method
        } else {
            method.asInitQuery()
        }

        return sendMessage(request)
    }

    private fun <T : Any> TLMethod<T>.asInitQuery(): TLMethod<*> {
        val initConnection = passport.toInitConnectionRequest(this)

        return com.attafitamim.mtproto.client.scheme.methods.global.TLInvokeWithLayer(
            passport.layer,
            initConnection,
            initConnection::parse
        )
    }

    private fun <T : Any> ConnectionPassport.toInitConnectionRequest(query: TLMethod<T>) =
        com.attafitamim.mtproto.client.scheme.methods.global.TLInitConnection(
            apiId,
            apiHash,
            deviceModel,
            systemVersion,
            appVersion,
            systemLangCode,
            langPack,
            langCode,
            proxy = null,
            query = query,
            parseX = query::parse
        )

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

    private suspend fun ConnectionSession.listenToMessage(): Flow<TLPublicMessage> =
        connection
            .listenToData()
            .mapNotNull { rawResponse ->
                val result = kotlin.runCatching {
                    val data = authenticator.unwrapData(session, rawResponse)
                    TLPublicMessage.parse(data.toInputStream())
                }

                val message = result.getOrNull()
                if (message != null) {
                    acknowledgeMessage(message.msgId)
                }

                message
            }

    private fun ConnectionSession.acknowledgeMessage(messageId: Long) = scope.launch {
        val messageIds = TLVector.Vector(listOf(messageId))
        val request = TLMsgsAck.MsgsAck(messageIds)
        sendMessage(request)
    }
}
