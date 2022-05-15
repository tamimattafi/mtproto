package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.behavior.TLParser
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable

interface TLMethod<R : Any> : TLSerializable, TLParser<R> {
    val constructorHash: Int
}
