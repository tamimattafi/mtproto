package com.attafitamim.mtproto.client.connection.core

import kotlinx.coroutines.flow.Flow

interface IConnection {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendData(byteArray: ByteArray)
    fun listenToData(): Flow<ByteArray>
    fun listenToEvents(): Flow<ConnectionEvent>
}
