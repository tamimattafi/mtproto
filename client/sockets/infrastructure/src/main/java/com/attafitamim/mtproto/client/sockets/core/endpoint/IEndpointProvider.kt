package com.attafitamim.mtproto.client.sockets.core.endpoint

fun interface IEndpointProvider {
    suspend fun provideEndpoint(retryCount: Int): Endpoint
}
