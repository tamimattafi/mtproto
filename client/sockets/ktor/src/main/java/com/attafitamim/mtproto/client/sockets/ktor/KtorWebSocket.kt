package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.BaseSocket
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.SocketEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KtorWebSocket(
    private val client: HttpClient,
    scope: CoroutineScope,
    connectRetryInterval: Long,
    maxRetryCount: Int,
    endpointProvider: IEndpointProvider
) : BaseSocket<DefaultWebSocketSession>(
    scope,
    connectRetryInterval,
    maxRetryCount,
    endpointProvider
) {

    override val isActive: Boolean
        get() = currentSession?.isActive == true

    override suspend fun writeText(data: String): Boolean {
        val frame = Frame.Text(data)
        return sendFrame(frame)
    }

    override suspend fun writeBytes(bytes: ByteArray): Boolean {
        val frame = Frame.Binary(fin = true, bytes)
        return sendFrame(frame)
    }

    override suspend fun createSession(endpoint: Endpoint): DefaultWebSocketSession =
        when (endpoint) {
            is Endpoint.Address -> client.webSocketSession(
                host = endpoint.host,
                port = endpoint.port,
                path = endpoint.path
            )

            is Endpoint.Url -> client.webSocketSession(endpoint.urlString)
        }

    override suspend fun forceClose() {
        currentSession?.close()
        currentSession = null
    }

    override fun DefaultWebSocketSession.handlePostInit() {
        launch {
            incoming.receiveAsFlow().collect(::handleIncomingFrame)
        }

        closeReason.awaitClose()
    }

    private suspend fun sendFrame(frame: Frame): Boolean {
        val result = startInternal {
            send(frame)
        }

        return result != null
    }

    private fun Deferred<CloseReason?>.awaitClose() = scope.launch {
        val reason = await()

        val closeEvent = when (val knownReason = reason?.knownReason) {
            null -> SocketEvent.Close.Unspecified
            CloseReason.Codes.NORMAL -> SocketEvent.Close.Graceful
            else -> SocketEvent.Close.Abnormal(
                knownReason.code,
                knownReason.name,
                reason.message
            )
        }

        emitEvent(closeEvent)
    }

    private suspend fun handleIncomingFrame(frame: Frame) {
        when (frame) {
            is Frame.Binary -> bytesFlow.emit(frame.data)
            is Frame.Text -> {
                val data = frame.data.toString(StandardCharsets.UTF_8)
                textFlow.emit(data)
            }
            else -> return
        }
    }
}
