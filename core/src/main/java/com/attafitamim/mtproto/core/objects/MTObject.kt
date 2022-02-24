package com.attafitamim.mtproto.core.objects

import com.attafitamim.mtproto.core.stream.MTOutputStream

interface MTObject {
    val hash: Int
    fun serialize(outputStream: MTOutputStream)
}