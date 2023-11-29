package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.core.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.core.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.obfuscation.toHex
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
    endpointProvider: IEndpointProvider
) : BaseSocket<Socket>(
    scope,
    connectRetryInterval,
    endpointProvider
) {

    override val isActive: Boolean
        get() = currentSession?.isActive == true

    @Volatile
    private var writeChannel: ByteWriteChannel? = null

    override fun writeText(data: String) {
        writeData(data.toByteArray())
    }

    override fun writeBytes(bytes: ByteArray) {
        writeData(bytes)
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
                println("READ_CHANNEL: trying to read")
                val readBytes = readChannel.readRemaining().readBytes()
                bytesFlow.emit(readBytes)
                println("READ_CHANNEL: READ: ${readBytes.toHex()}")
            }

            println("READ_CHANNEL: CLOSE")
        }

        //closeReason.awaitClose()
    }

    private fun writeData(data: ByteArray) {
        startInternal {
            requireNotNull(writeChannel).writeFully(data)
        }
    }
}