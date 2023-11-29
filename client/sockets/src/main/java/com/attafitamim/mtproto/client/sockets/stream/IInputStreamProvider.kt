package com.attafitamim.mtproto.client.sockets.stream

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

interface IInputStreamProvider {
    fun allocate(capacity: Int): TLInputStream

    fun wrap(byteArray: ByteArray): TLInputStream
}