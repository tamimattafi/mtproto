package com.attafitamim.mtproto.client.connection.core

interface IConnectionProvider {
    fun provideConnection(): IConnection
}