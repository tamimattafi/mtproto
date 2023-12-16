package com.attafitamim.mtproto.client.connection.auth

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.session.Session
import com.russhwolf.settings.Settings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DefaultAuthenticatorStorage(
    private val settings: Settings
) : IAuthenticatorStorage {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
        explicitNulls = true
    }

    override fun saveAuthCredentials(authCredentials: AuthCredentials) {
        val credentialsJson = json.encodeToString(authCredentials)
        settings.putString(KEY_CREDENTIALS, credentialsJson)
    }

    override fun getAuthCredentials(): AuthCredentials? {
        val credentialsJson = settings.getStringOrNull(KEY_CREDENTIALS) ?: return null
        return json.decodeFromString(credentialsJson)
    }

    override fun saveSession(connectionType: ConnectionType, session: Session) {
        val key = connectionType.toKey()
        val sessionJson = json.encodeToString(session)
        settings.putString(key, sessionJson)
    }

    override fun getSession(connectionType: ConnectionType): Session? {
        val key = connectionType.toKey()
        val sessionJson = settings.getStringOrNull(key) ?: return null
        return json.decodeFromString(sessionJson)
    }

    private fun ConnectionType.toKey(): String {
        val postfix = when (this) {
            is ConnectionType.Download -> SESSION_TYPE_DOWNLOAD
            is ConnectionType.Upload -> SESSION_TYPE_UPLOAD
            is ConnectionType.Generic -> buildString {
                append(SESSION_TYPE_GENERIC)

                val connectionKey = key
                if (!connectionKey.isNullOrBlank()) {
                    append(SEPARATOR, connectionKey)
                }
            }
        }

        return buildString {
            append(KEY_SESSION, SEPARATOR, postfix)
        }
    }

    private companion object {
        const val KEY_CREDENTIALS = "credentials"
        const val KEY_SESSION = "session"

        const val SESSION_TYPE_DOWNLOAD = "download"
        const val SESSION_TYPE_UPLOAD = "upload"
        const val SESSION_TYPE_GENERIC = "generic"

        const val SEPARATOR = "_"
    }
}