package com.attafitamim.mtproto.client.sockets.stream

import com.attafitamim.mtproto.client.sockets.buffer.ByteOrder
import com.attafitamim.mtproto.client.sockets.buffer.IByteBuffer
import com.attafitamim.mtproto.client.sockets.buffer.IByteBufferProvider
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_FALSE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_TRUE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_BITS_COUNT
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

class TLBufferedOutputStream(
    private val buffer: IByteBuffer
) : TLOutputStream {

    override var position: Int by buffer::position

    override fun writeByte(value: Byte): Int = writeToBuffer {
        putByte(value)
    }

    override fun writeInt(value: Int): Int = writeToBuffer {
        order(ByteOrder.LITTLE_ENDIAN)
        putInt(value)
        order(ByteOrder.BIG_ENDIAN)
    }

    override fun writeLong(value: Long): Int = writeToBuffer {
        order(ByteOrder.LITTLE_ENDIAN)
        putLong(value)
        order(ByteOrder.BIG_ENDIAN)
    }

    override fun writeDouble(value: Double): Int {
        val doubleAsLong = value.toRawBits()
        return writeLong(doubleAsLong)
    }

    override fun writeBoolean(value: Boolean): Int {
        val constructor = if (value) {
            BOOLEAN_CONSTRUCTOR_TRUE
        } else {
            BOOLEAN_CONSTRUCTOR_FALSE
        }

        return writeInt(constructor.toInt())
    }

    override fun writeString(value: String): Int {
        val stringBytes = value.toByteArray()
        return writeWrappedByteArray(stringBytes)
    }

    override fun writeByteArray(value: ByteArray): Int = writeToBuffer {
        buffer.putByteArray(value)
    }

    override fun writeInputStream(value: TLInputStream): Int {
        val bytes = value.readByteArray()
        return writeByteArray(bytes)
    }

    override fun rewind() {
        buffer.rewind()
    }

    override fun flip() {
        buffer.flip()
    }

    override fun toByteArray(clip: Boolean): ByteArray {
        if (clip) {
            flip()
        } else {
            rewind()
        }

        return buffer.getByteArray()
    }

    override fun writeIntAsBytes(
        value: Int,
        limit: Int
    ) = writeToBuffer {
        var writtenBytes = 0

        while (writtenBytes < limit) {
            val bitsPosition = writtenBytes * BYTE_BITS_COUNT
            val byte = (value shr bitsPosition) and 0xFF
            writtenBytes += writeByte(byte.toByte())
        }
    }

    /**
     * @see <a href="https://core.telegram.org/mtproto/serialize#base-types">Base Types</a>
     */
    override fun writeWrappedByteArray(value: ByteArray, appendPadding: Boolean) = writeToBuffer {

        if (value.size <= 253) {
            buffer.putByte(value.size.toByte())
        } else {
            buffer.putByte(254.toByte())
            buffer.putByte(value.size.toByte())
            buffer.putByte((value.size shr 8).toByte())
            buffer.putByte((value.size shr 16).toByte())
        }

        buffer.putByteArray(value)

        var i = if (value.size <= 253) 1 else 4
        while ((value.size + i) % 4 != 0) {
            buffer.putByte(0.toByte())
            i++
        }

        /*
        writeInt(value.size)
        writeByteArray(value)*/
    }

    private fun writeToBuffer(onWrite: IByteBuffer.() -> Unit): Int {
        val oldPosition = buffer.position
        buffer.onWrite()
        return buffer.position - oldPosition
    }

    class Provider(
        private val byteBufferProvider: IByteBufferProvider
    ) : IOutputStreamProvider {
        override fun allocate(capacity: Int): TLOutputStream {
            val buffer = byteBufferProvider.allocate(capacity)
            return TLBufferedOutputStream(buffer)
        }

        override fun wrap(byteArray: ByteArray): TLOutputStream {
            val buffer = byteBufferProvider.wrap(byteArray)
            return TLBufferedOutputStream(buffer)
        }
    }
}
