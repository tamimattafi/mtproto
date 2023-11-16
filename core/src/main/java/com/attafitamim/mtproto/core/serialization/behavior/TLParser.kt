package com.attafitamim.mtproto.core.serialization.behavior

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

fun interface TLParser<R : Any> {
    fun parse(inputStream: TLInputStream): R
}