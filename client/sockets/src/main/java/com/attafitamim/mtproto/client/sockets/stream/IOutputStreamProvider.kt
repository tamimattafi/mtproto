package com.attafitamim.mtproto.client.sockets.stream

import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

interface IOutputStreamProvider {
    fun allocate(capacity: Int): TLOutputStream

    fun wrap(byteArray: ByteArray): TLOutputStream
}