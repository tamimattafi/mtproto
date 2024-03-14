package com.attafitamim.mtproto.client.sample

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.connection.Endpoint
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.api.connection.IConnectionProvider
import com.attafitamim.mtproto.client.connection.auth.DefaultAuthenticationStorage
import com.attafitamim.mtproto.client.connection.interceptor.RequestLoggingInterceptor
import com.attafitamim.mtproto.client.connection.manager.ConnectionManager
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.connection.manager.IConnectionDelegate
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.sockets.ktor.KtorWebSocketConnection
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.russhwolf.settings.Settings
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ConnectionHelper {

    private val retryInterval = 40.seconds.inWholeMilliseconds
    private val maxRetryCount = 0


    const val WEB_SOCKET_URL = ""
    const val SERVER_IP = "127.0.0.1"
    const val SERVER_PORT = 2047

    private val serverKey = RsaKey.Raw(
        fingerprint = 1,
        type = RsaKey.Type.PUBLIC,
        modulusHex = "",
        exponentHex = "010001"
    )

    private val passport = ConnectionPassport(
        apiId = 1,
        apiHash = "",
        deviceModel = getPlatform().name,
        systemVersion = "1.2.3",
        appVersion = "playground-1",
        systemLangCode = "en",
        langPack = "en",
        langCode = "en",
        layer = 130
    )

    private val client by lazy {
        KtorModule.provideHttpClient()
    }

    val scope by lazy {
        CoroutineScope(Job() + Dispatchers.IO)
    }

    fun createConnectionManager(settings: Settings): IConnectionManager {
        val unknownMessageHandler = object : IConnectionDelegate {
            override fun onUnknownMessage(data: ByteArray) {
                println("CONNECTION: ${data.toHex()}")
            }

            override fun onSessionConnected(sessionId: Long, connectionType: ConnectionType) {
                println("CONNECTION: session(${sessionId.toULong()}) type($connectionType) connected")
            }
        }

        val logger = { message: String ->
            println("CONNECTION: $message")
        }

        val logInterceptor = RequestLoggingInterceptor(logger)

        val endpoint = createdEndpoint(WEB_SOCKET_URL)
        val connectionProvider = createConnectionProvider(endpoint)
        val storage = DefaultAuthenticationStorage(settings)
        val serverKeys = listOf(serverKey)
        return ConnectionManager(
            connectionProvider,
            storage,
            serverKeys,
            passport,
            unknownMessageHandler,
            listOf(logInterceptor),
            logger
        )
    }

    private fun createConnectionProvider(
        endpoint: Endpoint
    ) = IConnectionProvider {
        KtorWebSocketConnection(client, endpoint)
    }

    fun createdEndpoint(url: String) = Endpoint.Url(url)

    fun createdEndpoint(ip: String, port: Int) = Endpoint.Address(ip, port)
}
