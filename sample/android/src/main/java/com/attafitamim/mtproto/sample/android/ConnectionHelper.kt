package com.attafitamim.mtproto.sample.android

import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.sockets.connection.SocketConnectionManager
import com.attafitamim.mtproto.client.sockets.core.Endpoint
import com.attafitamim.mtproto.client.sockets.core.endpoint.IEndpointProvider
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

    private val scope by lazy {
        CoroutineScope(Job() + Dispatchers.IO)
    }

    fun createConnectionManager(): IConnectionManager {
        val endpointProvider = createdEndpointProvider()
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

    fun createdEndpointProvider() = IEndpointProvider {
        Endpoint.Url(BuildConfig.BACKEND_SOCKET_URL)
    }
}
