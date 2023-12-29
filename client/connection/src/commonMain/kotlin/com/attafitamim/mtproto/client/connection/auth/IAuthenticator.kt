package com.attafitamim.mtproto.client.connection.auth

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.connection.IConnection
import com.attafitamim.mtproto.client.connection.session.Session

interface IAuthenticator {

    suspend fun authenticate(
        connectionType: ConnectionType,
        connection: IConnection
    ): Session

    suspend fun cleanup()

    suspend fun wrapData(
        session: Session,
        data: ByteArray
    ): ByteArray

    suspend fun unwrapData(
        session: Session,
        data: ByteArray
    ): ByteArray
}
