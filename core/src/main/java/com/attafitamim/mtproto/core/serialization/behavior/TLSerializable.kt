package com.attafitamim.mtproto.core.serialization.behavior

import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

interface TLSerializable {
    fun serialize(outputStream: TLOutputStream)
}