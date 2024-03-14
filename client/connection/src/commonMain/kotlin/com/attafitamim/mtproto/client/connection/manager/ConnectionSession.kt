package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.session.Session
import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex

class ConnectionSession(
    val session: Session,
    val obfuscatedConnection: ObfuscatedConnection,
    val connectionType: ConnectionType,
    val connectionMutex: Mutex = Mutex(),

    @Volatile
    var isInitialized: Boolean = false,
    var isReconnecting: Boolean = false
)