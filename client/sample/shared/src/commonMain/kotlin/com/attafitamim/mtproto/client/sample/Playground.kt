package com.attafitamim.mtproto.client.sample

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.sample.ConnectionHelper.WEB_SOCKET_URL
import com.attafitamim.mtproto.client.scheme.methods.global.TLGetFutureSalts
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay

object Playground {

    suspend fun initConnection(settings: Settings) {
        val endpointProvider = ConnectionHelper.createdEndpointProvider(WEB_SOCKET_URL)
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)
        val connectionProvider = ConnectionHelper.createConnectionProvider(socketProvider)

        val passport = ConnectionPassport(
            apiId = 1234,
            apiHash = "1234",
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