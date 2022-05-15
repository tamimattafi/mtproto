package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable

interface TLObject : TLSerializable {
    val constructorHash: Int
}
