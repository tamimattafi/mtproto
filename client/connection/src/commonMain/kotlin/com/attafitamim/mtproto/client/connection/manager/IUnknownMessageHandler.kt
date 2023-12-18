package com.attafitamim.mtproto.client.connection.manager

fun interface IUnknownMessageHandler {
    fun handle(data: ByteArray)
}
