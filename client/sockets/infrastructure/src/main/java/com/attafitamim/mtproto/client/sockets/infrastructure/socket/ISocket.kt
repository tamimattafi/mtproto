package com.attafitamim.mtproto.client.sockets.infrastructure.socket

import kotlinx.coroutines.flow.Flow

interface ISocket {
    val isActive: Boolean
    suspend fun start()
    suspend fun writeText(data: String): Boolean
    suspend fun writeBytes(bytes: ByteArray): Boolean
    fun readText(): Flow<String>
    fun readBytes(): Flow<ByteArray>
    fun readEvents(): Flow<SocketEvent>
    suspend fun close()
}
