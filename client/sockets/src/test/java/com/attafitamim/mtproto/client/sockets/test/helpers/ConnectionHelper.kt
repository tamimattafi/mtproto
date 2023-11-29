package com.attafitamim.mtproto.client.sockets.test.helpers

import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.sockets.connection.SocketConnectionManager
import com.attafitamim.mtproto.client.sockets.core.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.core.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.ktor.KtorTcpSocketProvider
import com.attafitamim.mtproto.client.sockets.ktor.KtorWebSocket
import com.attafitamim.mtproto.client.sockets.ktor.KtorWebSocketProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ConnectionHelper {

    private val retryInterval = 40.seconds.inWholeMilliseconds

    private val client by lazy {
        KtorModule.provideHttpClient()
    }

    val scope by lazy {
        CoroutineScope(Job() + Dispatchers.IO)
    }

    fun createConnectionManager(
        endpointProvider: IEndpointProvider
    ): IConnectionManager {
        val socketProvider = createSocketProvider(endpointProvider)
        return SocketConnectionManager(scope, socketProvider)
    }

    fun createSocketProvider(
        endpointProvider: IEndpointProvider
    ) = KtorWebSocketProvider(
        client,
        scope,
        retryInterval,
        endpointProvider
    )

    fun createdEndpointProvider(url: String) = IEndpointProvider {
        Endpoint.Url(url)
    }

    fun createdEndpointProvider(ip: String, port: Int) = IEndpointProvider {
        Endpoint.Address(ip, port)
    }
}
