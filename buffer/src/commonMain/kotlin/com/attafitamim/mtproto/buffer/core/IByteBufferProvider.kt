package com.attafitamim.mtproto.buffer.core

interface IByteBufferProvider {
    fun allocate(capacity: Int): IByteBuffer
    fun wrap(byteArray: ByteArray): IByteBuffer
}