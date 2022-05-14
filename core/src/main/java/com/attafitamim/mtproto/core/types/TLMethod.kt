package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

interface TLMethod<R : Any> : TLObject {
    fun parse(inputStream: TLInputStream): R
}
