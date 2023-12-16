package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.BaseSocket
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KtorTcpSocket(
    scope: CoroutineScope,
    connectRetryInterval: Long,
    maxRetryCount: Int,
    endpointProvider: IEndpointProvider
) : BaseSocket<Socket>(
    scope,
    connectRetryInterval,
    maxRetryCount,
    endpointProvider
) {

    override val isActive: Boolean
        get() = currentSession?.isActive == true

    @Volatile
    private var writeChannel: ByteWriteChannel? = null

    override suspend fun writeText(data: String): Boolean {
        return writeData(data.toByteArray())
    }

    override suspend fun writeBytes(bytes: ByteArray): Boolean {
        return writeData(bytes)
    }

    override suspend fun createSession(endpoint: Endpoint): Socket =
        when (endpoint) {
            is Endpoint.Address -> {
                val socketBuilder = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                socketBuilder.connect(endpoint.host, requireNotNull(endpoint.port))
            }

            is Endpoint.Url -> {
                error("Url is not yet supported for tcp sockets")
            }
        }

    override suspend fun forceClose() {
        currentSession?.close()
        currentSession = null
    }

    override fun Socket.handlePostInit() {
        writeChannel = openWriteChannel(autoFlush = true)

        launch {
            val readChannel = openReadChannel()
            while (!readChannel.isClosedForRead) {
                val readBytes = readChannel.readRemaining().readBytes()
                bytesFlow.emit(readBytes)
            }
        }

        //closeReason.awaitClose()
    }

    private suspend fun writeData(data: ByteArray): Boolean {
        val result = startInternal {
            requireNotNull(writeChannel).writeFully(data)
        }

        return result != null
    }
}