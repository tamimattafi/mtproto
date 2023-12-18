package com.attafitamim.mtproto.client.connection.auth

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.session.Session

interface IAuthenticatorStorage {
    fun saveAuthCredentials(authCredentials: AuthCredentials)

    fun getAuthCredentials(): AuthCredentials?

    fun saveSession(connectionType: ConnectionType, session: Session)

    fun getSession(connectionType: ConnectionType): Session?
}