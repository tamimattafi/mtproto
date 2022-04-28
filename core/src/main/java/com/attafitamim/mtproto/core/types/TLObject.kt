package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

interface TLObject {
    val constructorHash: Int
    fun serialize(outputStream: TLOutputStream)
}