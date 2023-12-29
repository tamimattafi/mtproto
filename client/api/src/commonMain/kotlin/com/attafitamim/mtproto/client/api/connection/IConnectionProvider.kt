package com.attafitamim.mtproto.client.api.connection

fun interface IConnectionProvider {
    fun provideConnection(): IConnection
}
