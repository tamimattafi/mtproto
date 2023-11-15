package com.attafitamim.mtproto.client.sockets.core.endpoint

import com.attafitamim.mtproto.client.sockets.core.Endpoint

sealed interface IEndpointProvider {
    suspend fun provideEndpoint(): Endpoint
}
