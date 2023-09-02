package com.attafitamim.mtproto.client.api.events

import com.attafitamim.mtproto.client.api.bodies.RequestError
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import java.lang.Exception

interface IEventListener {
    fun onRequest(requestToken: Int, request: TLMethod<*>)
    fun onResponse(requestToken: Int, messageId: Int, request: TLMethod<*>, response: TLObject)
    fun onError(requestToken: Int, messageId: Int, request: TLMethod<*>, error: RequestError)
    fun onException(requestToken: Int, messageId: Int, request: TLMethod<*>, exception: Exception)
    fun onUpdate(update: TLObject)
    fun onConnectionStateChanged(state: Int)
    fun onSessionCreated()
}