package com.attafitamim.mtproto.core.objects

import com.attafitamim.mtproto.core.stream.MTInputStream

interface MTMethod<R : MTObject> : MTObject {
    fun parseResponse(inputStream: MTInputStream, hash: Int): R
}