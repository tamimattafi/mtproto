package com.attafitamim.mtproto.client.api.connection

import com.attafitamim.mtproto.client.api.bodies.RequestError
import java.lang.Exception

interface IRequestDelegate<T> {
    fun onResponse(response: T)
    fun onError(error: RequestError)
    fun onException(exception: Exception)
}