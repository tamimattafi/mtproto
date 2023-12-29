package com.attafitamim.mtproto.client.api.connection

import kotlinx.coroutines.flow.Flow

interface IConnection {
    val state: ConnectionState
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun sendData(byteArray: ByteArray): Boolean
    fun listenToData(): Flow<ByteArray>
    fun listenToState(): Flow<ConnectionState>
}
