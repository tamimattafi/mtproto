package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream

interface MTObject {
    val hash: Int
    fun serialize(outputStream: MTOutputStream)
}