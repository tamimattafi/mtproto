package com.attafitamim.mtproto.client.connection.core

import kotlinx.coroutines.flow.Flow

interface IConnection {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendData(byteArray: ByteArray)
    suspend fun listenToData(): Flow<ByteArray>
    suspend fun awaitClose()
}
