package com.attafitamim.mtproto.serialization.core

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

interface IInputStreamProvider {
    fun allocate(capacity: Int): TLInputStream

    fun wrap(byteArray: ByteArray): TLInputStream
}