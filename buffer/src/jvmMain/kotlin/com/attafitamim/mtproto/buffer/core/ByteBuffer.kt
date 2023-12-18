package com.attafitamim.mtproto.buffer.core

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import java.nio.ByteBuffer as PlatformBuffer

actual object ByteBuffer : IByteBufferProvider {

    override fun allocate(capacity: Int): IByteBuffer {
        val byteBuffer = PlatformBuffer.allocate(capacity)
        return JavaByteBuffer(byteBuffer)
    }

    override fun wrap(byteArray: ByteArray): IByteBuffer {
        val newBuffer = PlatformBuffer.wrap(byteArray)
        return JavaByteBuffer(newBuffer)
    }
}