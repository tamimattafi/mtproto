package com.attafitamim.mtproto.client.core

import com.attafitamim.mtproto.client.tgnet.RequestError
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject

interface IEventListener {
    fun onRequest(requestToken: Int, request: TLMethod<*>)
    fun onResponse(requestToken: Int, messageId: Int, request: TLMethod<*>, response: TLObject)
    fun onError(requestToken: Int, messageId: Int, request: TLMethod<*>, error: RequestError)
    fun onUpdate(update: TLObject)
}