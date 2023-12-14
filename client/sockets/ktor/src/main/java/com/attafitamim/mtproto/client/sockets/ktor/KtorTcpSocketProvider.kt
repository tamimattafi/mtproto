package com.attafitamim.mtproto.client.sockets.ktor

import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocket
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocketProvider
import kotlinx.coroutines.CoroutineScope

class KtorTcpSocketProvider(
    private val scope: CoroutineScope,
    private val connectRetryInterval: Long,
    private val endpointProvider: IEndpointProvider
) : ISocketProvider {

    override fun provideSocket(): ISocket =
        KtorTcpSocket(
            scope,
            connectRetryInterval,
            endpointProvider
        )
}
