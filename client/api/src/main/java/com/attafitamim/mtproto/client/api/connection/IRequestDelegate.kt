package com.attafitamim.mtproto.client.api.connection

import com.attafitamim.mtproto.client.api.bodies.RequestError

fun interface IRequestDelegate<T> {
    fun run(response: T?, error: RequestError?)
}