package com.attafitamim.mtproto.sample.shared

import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.connection.auth.DefaultAuthenticator
import com.attafitamim.mtproto.client.connection.auth.DefaultAuthenticatorStorage
import com.attafitamim.mtproto.client.connection.auth.IAuthenticator
import com.attafitamim.mtproto.client.connection.core.IConnectionProvider
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.connection.manager.DefaultConnectionManager
import com.attafitamim.mtproto.client.connection.manager.IUnknownMessageHandler
import com.attafitamim.mtproto.client.connection.utils.toHex
import com.attafitamim.mtproto.client.sockets.connection.SocketConnectionProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.SimpleEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocketProvider
import com.attafitamim.mtproto.client.sockets.ktor.KtorWebSocketProvider
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.russhwolf.settings.Settings
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ConnectionHelper {

    private val retryInterval = 40.seconds.inWholeMilliseconds
    private val maxRetryCount = 0

    private val client by lazy {
        KtorModule.provideHttpClient()
    }

    val scope by lazy {
        CoroutineScope(Job() + Dispatchers.IO)
    }

    fun createConnectionManager(
        connectionProvider: IConnectionProvider,
        passport: ConnectionPassport,
        authenticator: IAuthenticator
    ): IConnectionManager {
        val unknownMessageHandler =
            IUnknownMessageHandler { data -> println("IUnknownMessageHandler: ${data.toHex()}") }

        return DefaultConnectionManager(scope, connectionProvider, authenticator, unknownMessageHandler, passport)
    }

    fun createSocketProvider(
        endpointProvider: IEndpointProvider
    ) = KtorWebSocketProvider(
        client,
        scope,
        retryInterval,
        maxRetryCount,
        endpointProvider
    )

    fun createConnectionProvider(
        socketProvider: ISocketProvider
    ) = SocketConnectionProvider(scope, socketProvider)

    fun createAuthenticator(settings: Settings): IAuthenticator {
        val storage = DefaultAuthenticatorStorage(settings)



        val serverKeys = listOf(serverKey)
        return DefaultAuthenticator(storage, serverKeys)
    }

    fun createdEndpointProvider(url: String) = SimpleEndpointProvider(Endpoint.Url(url))

    fun createdEndpointProvider(ip: String, port: Int) = SimpleEndpointProvider(Endpoint.Address(ip, port))
}
