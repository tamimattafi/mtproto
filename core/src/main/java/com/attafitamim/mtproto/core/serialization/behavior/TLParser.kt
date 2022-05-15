package com.attafitamim.mtproto.core.serialization.behavior

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

interface TLParser<R : Any> {
    fun parse(inputStream: TLInputStream): R
}