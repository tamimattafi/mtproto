package com.attafitamim.mtproto.client.sockets.infrastructure.endpoint

class SimpleEndpointProvider(private val endpoint: Endpoint) : IEndpointProvider {

    override suspend fun provideEndpoint(retryCount: Int): Endpoint = endpoint
}
