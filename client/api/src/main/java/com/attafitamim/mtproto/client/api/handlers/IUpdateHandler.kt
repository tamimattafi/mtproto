package com.attafitamim.mtproto.client.api.handlers

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.types.TLObject

fun interface IUpdateHandler {
    fun parseUpdate(inputStream: TLInputStream): TLObject
}
