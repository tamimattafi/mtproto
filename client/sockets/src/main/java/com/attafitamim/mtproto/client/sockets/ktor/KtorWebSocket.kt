package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.core.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.core.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.core.socket.ISocket
import com.attafitamim.mtproto.client.sockets.core.socket.SocketEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class KtorWebSocket(
    private val client: HttpClient,
    private val scope: CoroutineScope,
    private val connectRetryInterval: Long,
    private val endpointProvider: IEndpointProvider
) : ISocket {

    override val isActive: Boolean
        get() = currentSession?.isActive == true

    @Volatile
    private var currentSession: WebSocketSession? = null
    private val retryCount = AtomicInteger(0)
    private val mutex = Mutex()

    private val textFlow = MutableSharedFlow<String>()
    private val bytesFlow = MutableSharedFlow<ByteArray>()
    private val eventsFlow = MutableSharedFlow<SocketEvent>()

    override fun start(onStart: suspend ISocket.() -> Unit) {
        startInternal {
            onStart()
        }
    }

    override fun writeText(data: String) {
        val frame = Frame.Text(data)
        sendFrame(frame)
    }

    override fun writeBytes(bytes: ByteArray) {
        val frame = Frame.Binary(fin = true, bytes)
        sendFrame(frame)
    }

    override fun readText(): Flow<String> =
        textFlow.asSharedFlow()

    override fun readBytes(): Flow<ByteArray> =
        bytesFlow.asSharedFlow()

    override fun readEvents(): Flow<SocketEvent> =
        eventsFlow.asSharedFlow()

    override fun close() {
        scope.coroutineContext.cancelChildren()
        scope.launch {
            forceClose()
        }
    }

    private fun sendFrame(frame: Frame) {
        startInternal {
            send(frame)
        }
    }

    private fun startInternal(onStart: suspend WebSocketSession.() -> Unit) {
        provideSession {
            launch {
                onStart()
            }
        }
    }

    private fun provideSession(
        onProvide: suspend WebSocketSession.() -> Unit = {}
    ) = scope.launch {
        mutex.lock()
        val session = provideSession()
        mutex.unlock()
        onProvide.invoke(session)
    }

    private suspend fun provideSession(): WebSocketSession {
        val session = this@KtorWebSocket.currentSession
        if (session != null && session.isActive) {
            return session
        }

        forceClose()

        // Keep retrying to connect to the socket
        val endpoint = endpointProvider.provideEndpoint()
        var newSession = tryCreateSession(endpoint)
        while (newSession == null) {
            retryCount.incrementAndGet()
            delay(connectRetryInterval)
            newSession = tryCreateSession(endpoint)
        }

        retryCount.set(0)
        currentSession = newSession
        newSession.handlePostInit()
        emitEvent(SocketEvent.Connected)
        return newSession
    }

    private suspend fun tryCreateSession(endpoint: Endpoint): DefaultWebSocketSession? =
        kotlin.runCatching {
            createSession(endpoint)
        }.onFailure { error ->
            val errorEvent = SocketEvent.Error.NoConnection(
                error,
                retryCount.get()
            )

            emitEvent(errorEvent)
        }.getOrNull()

    private suspend fun createSession(endpoint: Endpoint): DefaultWebSocketSession =
        when (endpoint) {
            is Endpoint.Address -> client.webSocketSession(
                host = endpoint.host,
                port = endpoint.port,
                path = endpoint.path
            )

            is Endpoint.Url -> client.webSocketSession(endpoint.urlString)
        }

    private suspend fun forceClose() {
        currentSession?.close()
        currentSession = null
    }

    private fun DefaultWebSocketSession.handlePostInit() {
        launch {
            incoming.receiveAsFlow().collect(::handleIncomingFrame)
        }

        closeReason.awaitClose()
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

    private fun emitEvent(socketEvent: SocketEvent) = scope.launch {
        eventsFlow.emit(socketEvent)
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
