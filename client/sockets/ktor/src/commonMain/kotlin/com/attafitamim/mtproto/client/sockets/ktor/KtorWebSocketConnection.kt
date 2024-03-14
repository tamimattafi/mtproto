package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.api.connection.ConnectionState
import com.attafitamim.mtproto.client.api.connection.Endpoint
import com.attafitamim.mtproto.client.api.connection.IConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class KtorWebSocketConnection(
    private val client: HttpClient,
    endpoint: Endpoint
) : BaseSocketConnection<DefaultWebSocketSession>(endpoint), IConnection {

    override suspend fun DefaultWebSocketSession.sendData(byteArray: ByteArray) {
        val frame = Frame.Binary(fin = true, byteArray)
        outgoing.send(frame)
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

    override suspend fun forceDisconnect() {
        currentSession?.close()
        currentSession = null
    }

    override fun DefaultWebSocketSession.handlePostInit() {
        launch {
            incoming.receiveAsFlow().collect(::handleIncomingFrame)
        }

        closeReason.awaitClose()
    }

    private fun Deferred<CloseReason?>.awaitClose() = scope.launch {
        val closeReason = await()
        println("CONNECTION: close reason $closeReason")
        emitEvent(ConnectionState.Disconnected)
    }

    private suspend fun handleIncomingFrame(frame: Frame) {
        when (frame) {
            is Frame.Binary -> bytesFlow.emit(frame.data)
            else -> return
        }
    }
}
