package com.attafitamim.mtproto.client.sockets.core.socket

fun interface ISocketProvider {

    fun provideSocket(): ISocket
}