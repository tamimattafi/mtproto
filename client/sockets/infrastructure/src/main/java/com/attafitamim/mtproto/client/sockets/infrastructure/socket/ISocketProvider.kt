package com.attafitamim.mtproto.client.sockets.infrastructure.socket

fun interface ISocketProvider {

    fun provideSocket(): ISocket
}
