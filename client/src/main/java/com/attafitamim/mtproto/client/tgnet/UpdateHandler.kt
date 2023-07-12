package com.attafitamim.mtproto.client.tgnet

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.types.TLObject

fun interface UpdateHandler {
    fun parseUpdate(inputStream: TLInputStream): TLObject
}