package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocket
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.SocketEvent
import com.attafitamim.mtproto.security.obfuscation.IObfuscator
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.utils.serializeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SocketConnection(
    private val scope: CoroutineScope,
    private val obfuscator: IObfuscator,
    private val socket: ISocket
) : IConnection {

    private val closeFlow = MutableSharedFlow<SocketEvent>()
    private val mutex = Mutex()

    override suspend fun connect() = mutex.withLock {
        socket.start()

        val initData = obfuscator.init()
        writePacket(initData)

        handleEvents()

        // delay because server doesn't send a notification of receiving init data
        // TODO: maybe fix this?
        delay(2000)
    }

    private fun handleEvents() = scope.launch {
        socket.readEvents().collect { event ->
            println("CONNECTION: event $event")
            when (event) {
                is SocketEvent.Close,
                is SocketEvent.Error -> {
                    obfuscator.release()
                    closeFlow.emit(event)
                }

                is SocketEvent.Connected -> return@collect
            }
        }
    }

    override suspend fun disconnect() = mutex.withLock {
        socket.close()
        obfuscator.release()
    }

    override suspend fun sendData(byteArray: ByteArray) = mutex.withLock {
        val packetBytes = serializeData {
            writeInt(byteArray.size)
            writeByteArray(byteArray)
        }

        val obfuscatedBytes = obfuscator.obfuscate(packetBytes)
        writePacket(obfuscatedBytes)
    }

    override suspend fun listenToData(): Flow<ByteArray> = socket.readBytes().map { data ->
        println("CONNECTION: raw data ${data.size} bytes")
        val clarifiedBytes = obfuscator.clarify(data)
        println("CONNECTION: clarifiedBytes data ${data.size} bytes")
        val responseBuffer = JavaByteBuffer.wrap(clarifiedBytes)
        val inputStream = TLBufferedInputStream(responseBuffer)
        val size = inputStream.readInt()
        println("CONNECTION: protocol data size $size")
        inputStream.readBytes(size)
    }

    override suspend fun awaitClose() {
        closeFlow.asSharedFlow().first()
    }

    private fun writePacket(packet: ByteArray) {
        socket.writeBytes(packet)
    }
 }
