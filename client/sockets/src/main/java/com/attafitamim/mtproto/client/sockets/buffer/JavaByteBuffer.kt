package com.attafitamim.mtproto.client.sockets.buffer

import java.nio.ByteBuffer

class JavaByteBuffer(
    private val byteBuffer: ByteBuffer
) : IByteBuffer {

    override var position: Int
        get() = byteBuffer.position()
        set(value) {
            byteBuffer.position(value)
        }

    override val remaining: Int
        get() = byteBuffer.remaining()

    override fun rewind() {
        byteBuffer.rewind()
    }

    override fun putByte(byte: Byte) {
        byteBuffer.put(byte)
    }

    override fun putByteArray(byteArray: ByteArray) {
        byteBuffer.put(byteArray)
    }

    override fun putInt(value: Int) {
        byteBuffer.putInt(value)
    }

    override fun putLong(value: Long) {
        byteBuffer.putLong(value)
    }

    override fun getByte(): Byte =
        byteBuffer.get()

    override fun getByteArray(limit: Int): ByteArray {
        val bytes = ByteArray(limit)
        byteBuffer.get(bytes)
        return bytes
    }

    override fun getByteArray(): ByteArray =
        byteBuffer.array()

    override fun fillByteArray(destination: ByteArray, offset: Int, limit: Int) {
        byteBuffer[destination, offset, limit]
    }

    override fun getInt(): Int =
        byteBuffer.int

    override fun getLong(): Long =
        byteBuffer.long

    override fun order(byteOrder: ByteOrder) {
        when (byteOrder) {
            ByteOrder.BIG_ENDIAN -> byteBuffer.order(java.nio.ByteOrder.BIG_ENDIAN)
            ByteOrder.LITTLE_ENDIAN -> byteBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        }
    }

    override fun wrap(byteArray: ByteArray): IByteBuffer {
        val newBuffer = ByteBuffer.wrap(byteArray)
        return JavaByteBuffer(newBuffer)
    }

    companion object {

        fun allocate(capacity: Int): IByteBuffer {
            val byteBuffer = ByteBuffer.allocate(capacity)
            return JavaByteBuffer(byteBuffer)
        }
    }
}
