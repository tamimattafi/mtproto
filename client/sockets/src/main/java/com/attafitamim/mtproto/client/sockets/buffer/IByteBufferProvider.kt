package com.attafitamim.mtproto.client.sockets.buffer

interface IByteBufferProvider {
    fun allocate(capacity: Int): IByteBuffer
    fun wrap(byteArray: ByteArray): IByteBuffer
}