package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.session.Session

class ConnectionSession(
    val session: Session,
    val connection: IConnection,
    val connectionType: ConnectionType,
    var isInitialized: Boolean = false
)