package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.api.connection.ConnectionState
import com.attafitamim.mtproto.client.api.connection.Endpoint
import com.attafitamim.mtproto.client.api.connection.IConnection
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class BaseSocketConnection<S : CoroutineScope>(
    private val endpoint: Endpoint
): IConnection {

    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val state: ConnectionState
        get() = stateFlow.value

    @Volatile
    protected var currentSession: S? = null

    protected val mutex = Mutex()
    protected val bytesFlow = MutableSharedFlow<ByteArray>()
    protected val stateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)

    protected abstract suspend fun createSession(endpoint: Endpoint): S
    protected abstract suspend fun forceDisconnect()
    protected abstract suspend fun S.sendData(byteArray: ByteArray)
    protected abstract fun S.handlePostInit()

    override suspend fun connect(): Boolean = mutex.withLock {
        val session = this@BaseSocketConnection.currentSession
        if (session != null && session.isActive) {
            return true
        }

        val result = kotlin.runCatching {
            forceDisconnect()

            val newSession = createSession(endpoint)
            currentSession = newSession

            newSession.handlePostInit()
            emitEvent(ConnectionState.Connected)
        }

        result.isSuccess
    }

    override suspend fun sendData(byteArray: ByteArray): Boolean = mutex.withLock {
        val session = currentSession?.takeIf(CoroutineScope::isActive)
            ?: return false

        val result = kotlin.runCatching {
            session.sendData(byteArray)
        }

        result.isSuccess
    }

    override fun listenToData(): Flow<ByteArray> =
        bytesFlow.asSharedFlow()

    override fun listenToState(): Flow<ConnectionState> =
        stateFlow.asSharedFlow()

    override suspend fun disconnect() = mutex.withLock {
        scope.coroutineContext.cancelChildren()
        forceDisconnect()
    }

    protected fun emitEvent(connectionEvent: ConnectionState) = scope.launch {
        stateFlow.emit(connectionEvent)
    }
}
