package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.session.Session
import kotlin.concurrent.Volatile

class ConnectionSession(
    val session: Session,
    val obfuscatedConnection: ObfuscatedConnection,
    val connectionType: ConnectionType,

    @Volatile
    var isInitialized: Boolean = false
)