package com.attafitamim.mtproto.core.objects

import com.attafitamim.mtproto.core.stream.MTOutputStream

abstract class MTObject {
    abstract val hash: Int
    abstract fun serialize(outputStream: MTOutputStream)
}