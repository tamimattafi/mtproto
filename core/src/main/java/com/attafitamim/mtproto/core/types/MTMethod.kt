package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.MTInputStream

interface MTMethod<R : MTObject> : MTObject {
    fun parse(inputStream: MTInputStream, constructorHash: Int): R
}