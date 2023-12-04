package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.core.types.TLMethod

interface IConnection {
    suspend fun connect()
    suspend fun <R : Any> sendRequest(request: TLMethod<R>): R
}
