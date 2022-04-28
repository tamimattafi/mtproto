package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

interface TLMethod<R : TLObject> : TLObject {
    fun parse(inputStream: TLInputStream, constructorHash: Int): R
}