package com.attafitamim.mtproto.client.sockets.infrastructure.endpoint

interface IEndpointProvider {
    suspend fun provideEndpoint(retryCount: Int): Endpoint
}
