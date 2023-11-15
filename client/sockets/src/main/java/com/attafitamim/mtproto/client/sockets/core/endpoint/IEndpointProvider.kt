package com.attafitamim.mtproto.client.sockets.core.endpoint

import com.attafitamim.mtproto.client.sockets.core.Endpoint

fun interface IEndpointProvider {
    suspend fun provideEndpoint(): Endpoint
}
