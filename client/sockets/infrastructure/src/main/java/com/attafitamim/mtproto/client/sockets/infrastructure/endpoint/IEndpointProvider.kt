package com.attafitamim.mtproto.client.sockets.infrastructure.endpoint

fun interface IEndpointProvider {
    suspend fun provideEndpoint(retryCount: Int): Endpoint
}
