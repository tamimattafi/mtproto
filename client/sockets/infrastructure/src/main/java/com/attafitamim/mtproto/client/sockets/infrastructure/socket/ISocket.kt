package com.attafitamim.mtproto.client.sockets.infrastructure.socket

import kotlinx.coroutines.flow.Flow

interface ISocket {
    val isActive: Boolean
    fun start(onStart: (suspend ISocket.() -> Unit) = {})
    fun writeText(data: String)
    fun writeBytes(bytes: ByteArray)
    fun readText(): Flow<String>
    fun readBytes(): Flow<ByteArray>
    fun readEvents(): Flow<SocketEvent>
    fun close()
}
