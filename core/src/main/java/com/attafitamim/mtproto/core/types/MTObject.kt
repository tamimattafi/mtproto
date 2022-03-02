package com.attafitamim.mtproto.core.types

import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream

interface MTObject {
    val constructorHash: Int
    fun serialize(outputStream: MTOutputStream)
}