package com.attafitamim.mtproto.core.objects

import com.attafitamim.mtproto.core.stream.MTInputStream

abstract class MTMethod<R : MTObject> : MTObject() {
    abstract fun parseResponse(inputStream: MTInputStream, hash: Boolean): R
}