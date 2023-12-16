package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocket
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocketProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

class KtorWebSocketProvider(
    private val client: HttpClient,
    private val scope: CoroutineScope,
    private val connectRetryInterval: Long,
    private val maxRetryCount: Int,
    private val endpointProvider: IEndpointProvider
) : ISocketProvider {

    override fun provideSocket(): ISocket =
        KtorWebSocket(
            client,
            scope,
            connectRetryInterval,
            maxRetryCount,
            endpointProvider
        )
}
