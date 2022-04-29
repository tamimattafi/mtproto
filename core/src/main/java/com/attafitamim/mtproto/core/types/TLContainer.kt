package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

interface TLContainer {
    fun serialize(outputStream: TLOutputStream)
}