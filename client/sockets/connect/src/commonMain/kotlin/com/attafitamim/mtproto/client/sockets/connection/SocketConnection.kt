package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.client.connection.core.ConnectionEvent
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocket
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.SocketEvent
import com.attafitamim.mtproto.security.obfuscation.IObfuscator
import com.attafitamim.mtproto.serialization.utils.serializeData
import com.attafitamim.mtproto.serialization.utils.toTLInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SocketConnection(
    private val scope: CoroutineScope,
    private val obfuscator: IObfuscator,
    private val socket: ISocket
) : IConnection {

    private val dataFlow = MutableSharedFlow<ByteArray>()
    private val eventsFlow = MutableSharedFlow<ConnectionEvent>()

    private val dataSendingQueue = ArrayDeque<ByteArray>()
    private val mutex = Mutex()

    init {
        handleEvents()
        handleData()
    }

    override suspend fun connect() {
        mutex.withLock {
            socket.start()
        }
    }

    private suspend fun initObfuscator() = mutex.withLock {
        val initData = obfuscator.init()
        if (!writePacket(initData)) {
            disconnect()
            return@withLock
        }

        eventsFlow.emit(ConnectionEvent.Connected)
        startDataSending()
    }

    private suspend fun startDataSending() {
        while (dataSendingQueue.isNotEmpty() && obfuscator.isInitialized() && socket.isActive) {
            val byteArray = dataSendingQueue.first()

            val packetBytes = serializeData {
                writeInt(byteArray.size)
                writeByteArray(byteArray)
            }

            val obfuscatedBytes = obfuscator.obfuscate(packetBytes)
            if (writePacket(obfuscatedBytes)) {
                dataSendingQueue.removeFirst()
            } else {
                break
            }
        }
    }

    override suspend fun disconnect() = mutex.withLock {
        socket.close()
    }

    override suspend fun sendData(byteArray: ByteArray) = mutex.withLock {
        dataSendingQueue.add(byteArray)
        tryStartDataSending()
    }

    private suspend fun tryStartDataSending() {
        if (obfuscator.isInitialized()) {
            startDataSending()
        }
    }

    override fun listenToData(): Flow<ByteArray> =
        dataFlow.asSharedFlow()

    override fun listenToEvents(): Flow<ConnectionEvent> =
        eventsFlow.asSharedFlow()

    private suspend fun writePacket(packet: ByteArray): Boolean {
        println("CONNECTION: ${this@SocketConnection.hashCode()} writing packet ${packet.toHex()}")
        return socket.writeBytes(packet)
    }

    private fun handleData() = scope.launch {
        socket.readBytes().collect { data ->
            println("CONNECTION: ${this@SocketConnection.hashCode()} raw data ${data.size} bytes")
            val clarifiedBytes = obfuscator.clarify(data)
            println("CONNECTION: ${this@SocketConnection.hashCode()} clarifiedBytes data ${data.size} bytes")
            val inputStream = clarifiedBytes.toTLInputStream()
            val size = inputStream.readInt()
            println("CONNECTION: ${this@SocketConnection.hashCode()} protocol data size $size")
            val response = inputStream.readBytes(size)
            dataFlow.emit(response)
        }
    }

    private fun handleEvents() = scope.launch {
        socket.readEvents().collect { event ->
            println("CONNECTION: ${this@SocketConnection.hashCode()} socket event $event")
            when (event) {
                is SocketEvent.Close -> {
                    obfuscator.release()
                    eventsFlow.emit(ConnectionEvent.Disconnected)
                    delay(3000L)
                    connect()
                }

                is SocketEvent.Connected -> {
                    initObfuscator()
                }

                is SocketEvent.Error.MaxConnectRetriesReached -> {
                    eventsFlow.emit(ConnectionEvent.ConnectionError)
                    delay(3000L)
                    connect()
                }

                is SocketEvent.Error.NoConnection -> return@collect
            }
        }
    }
 }
