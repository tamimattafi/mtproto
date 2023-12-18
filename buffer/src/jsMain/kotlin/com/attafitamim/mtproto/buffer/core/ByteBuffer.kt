package com.attafitamim.mtproto.buffer.core

actual object ByteBuffer : IByteBufferProvider {

    override fun allocate(capacity: Int): IByteBuffer = TODO()
    override fun wrap(byteArray: ByteArray): IByteBuffer = TODO()
}
