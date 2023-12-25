package com.attafitamim.mtproto.sample.shared

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.scheme.methods.global.TLGetFutureSalts
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay

object Playground {

    private const val WEB_SOCKET_URL =
    private const val SERVER_IP = "127.0.0.1"
    private const val SERVER_PORT = 2047

    suspend fun initConnection(settings: Settings) {
        val endpointProvider = ConnectionHelper.createdEndpointProvider(WEB_SOCKET_URL)
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)
        val connectionProvider = ConnectionHelper.createConnectionProvider(socketProvider)

        val passport = ConnectionPassport(
            apiId = ,
            apiHash = ,
            deviceModel = getPlatform().name,
            systemVersion = "1.2.3",
            appVersion = "playground-1",
            systemLangCode = "en",
            langPack = "en",
            langCode = "en",
            layer = 130
        )

        val authenticator = ConnectionHelper.createAuthenticator(settings)
        val connectionManager = ConnectionHelper.createConnectionManager(
            connectionProvider,
            passport,
            authenticator
        )

        val connectionType = ConnectionType.Generic("calls")
        connectionManager.initConnection(connectionType)

        repeat(10) {
            kotlin.runCatching {
                // Generic connection
                val getSalts = TLGetFutureSalts(1)
                val salts = connectionManager.sendRequest(getSalts, connectionType)
                println("TLGetFutureSalts: $salts")
            }

            delay(3000)

            kotlin.runCatching {
                // Download connection
                val getSalts = TLGetFutureSalts(1)
                val salts = connectionManager.sendRequest(getSalts, ConnectionType.Download)
                println("TLGetFutureSalts: $salts")
            }

            delay(3000)
        }
    }
}