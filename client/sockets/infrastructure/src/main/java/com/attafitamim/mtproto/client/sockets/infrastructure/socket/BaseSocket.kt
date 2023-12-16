package com.attafitamim.mtproto.client.sockets.infrastructure.socket

import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
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
    protected val maxRetryCount: Int,
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

    override suspend fun start() {
        safeProvideSession()
    }

    override fun readText(): Flow<String> =
        textFlow.asSharedFlow()

    override fun readBytes(): Flow<ByteArray> =
        bytesFlow.asSharedFlow()

    override fun readEvents(): Flow<SocketEvent> =
        eventsFlow.asSharedFlow()

    override suspend fun close() {
        mutex.lock()
        scope.coroutineContext.cancelChildren()

        val closingTask = scope.async {
            forceClose()
        }

        closingTask.await()
        mutex.unlock()
    }

    protected suspend fun <T : Any> startInternal(
        onStart: suspend S.() -> T
    ): T? {
        val session = safeProvideSession() ?: return null

        val startTask = session.async {
            session.onStart()
        }

        return startTask.await()
    }

    protected suspend fun safeProvideSession(): S? {
        mutex.lock()
        val providingSession = scope.async {
            provideSession()
        }

        val session = providingSession.await()
        mutex.unlock()

        return session
    }

    protected suspend fun provideSession(): S? {
        val session = this@BaseSocket.currentSession
        val isActive = this@BaseSocket.isActive
        if (session != null && isActive) {
            return session
        }

        forceClose()

        // Keep retrying to connect to the socket
        val endpoint = endpointProvider.provideEndpoint(retryCount.get())
        var newSession = tryCreateSession(endpoint)

        while (newSession == null && retryCount.get() < maxRetryCount) {
            retryCount.incrementAndGet()
            delay(connectRetryInterval)
            newSession = tryCreateSession(endpoint)
        }

        retryCount.set(0)
        currentSession = newSession

        val event = if (newSession != null) {
            newSession.handlePostInit()
            SocketEvent.Connected
        } else {
            SocketEvent.Error.MaxConnectRetriesReached(maxRetryCount)
        }

        emitEvent(event)
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
