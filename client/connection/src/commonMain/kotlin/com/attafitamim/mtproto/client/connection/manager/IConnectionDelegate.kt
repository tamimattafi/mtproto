package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType

interface IConnectionDelegate {
    fun onUnknownMessage(data: ByteArray)
    fun onSessionConnected(sessionId: Long, connectionType: ConnectionType)
}
