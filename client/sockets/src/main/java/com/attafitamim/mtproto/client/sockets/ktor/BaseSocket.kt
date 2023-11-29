package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.core.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.core.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.core.socket.ISocket
import com.attafitamim.mtproto.client.sockets.core.socket.SocketEvent
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

abstract class BaseSocket<S : CoroutineScope>(
    protected val scope: CoroutineScope,
    protected val connectRetryInterval: Long,
    protected val endpointProvider: IEndpointProvider
) : ISocket {

    @Volatile
    protected var currentSession: S? = null
    protected val retryCount = AtomicInteger(0)
    protected val mutex = Mutex()

    protected val textFlow = MutableSharedFlow<String>()
    protected val bytesFlow = MutableSharedFlow<ByteArray>()
    protected val eventsFlow = MutableSharedFlow<SocketEvent>()

    protected abstract suspend fun createSession(endpoint: Endpoint): S
    protected abstract suspend fun forceClose()
    protected abstract fun S.handlePostInit()

    override fun start(onStart: suspend ISocket.() -> Unit) {
        startInternal {
            onStart()
        }
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

    protected fun startInternal(onStart: suspend S.() -> Unit) {
        provideSession {
            scope.launch {
                onStart()
            }
        }
    }

    protected fun provideSession(
        onProvide: suspend S.() -> Unit = {}
    ) = scope.launch {
        mutex.lock()
        val session = provideSession()
        mutex.unlock()
        onProvide.invoke(session)
    }

    protected suspend fun provideSession(): S {
        val session = this@BaseSocket.currentSession
        val isActive = this@BaseSocket.isActive
        if (session != null && isActive) {
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

    protected suspend fun tryCreateSession(endpoint: Endpoint): S? =
        kotlin.runCatching {
            createSession(endpoint)
        }.onFailure { error ->
            val errorEvent = SocketEvent.Error.NoConnection(
                error,
                retryCount.get()
            )

            emitEvent(errorEvent)
        }.getOrNull()

    protected fun emitEvent(socketEvent: SocketEvent) = scope.launch {
        eventsFlow.emit(socketEvent)
    }
}
