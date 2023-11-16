package com.attafitamim.mtproto.client.sockets.stream

import com.attafitamim.mtproto.client.sockets.buffer.ByteOrder
import com.attafitamim.mtproto.client.sockets.buffer.IByteBuffer
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_FALSE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_TRUE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_BITS_COUNT
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_SIZE_DIVISOR
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_SLOT_SIZE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.WRAPPED_BYTES_MAX_LENGTH
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream

class TLBufferedOutputStream(
    private val buffer: IByteBuffer
) : TLOutputStream {

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
        return writeWrappedBytes(stringBytes)
    }

    override fun writeByteArray(value: ByteArray): Int = writeToBuffer {
        buffer.putByteArray(value)
    }

    override fun writeInputStream(value: TLInputStream): Int {
        val bytes = value.readByteArray()
        return writeByteArray(bytes)
    }

    private fun writeIntAsBytes(
        value: Int,
        limit: Int = BYTE_SLOT_SIZE
    ) = writeToBuffer {
        var writtenBytes = 0

        while (writtenBytes < limit) {
            val bitsPosition = writtenBytes * BYTE_BITS_COUNT
            val byte = (value shr bitsPosition).toByte()
            writtenBytes += writeByte(byte)
        }
    }

    /**
     * @see <a href="https://core.telegram.org/mtproto/serialize#base-types">Base Types</a>
     */
    private fun writeWrappedBytes(value: ByteArray) = writeToBuffer {
        val oldPosition = buffer.position

        val size = value.size
        if (size >= WRAPPED_BYTES_MAX_LENGTH) {
            writeIntAsBytes(WRAPPED_BYTES_MAX_LENGTH)
            writeIntAsBytes(size, limit = 3)
        } else {
            writeIntAsBytes(size)
        }

        writeByteArray(value)

        val writtenBytes = buffer.position - oldPosition
        val writtenBytesRemainder = writtenBytes % BYTE_SIZE_DIVISOR
        if (writtenBytesRemainder > 0) {
            val offsetBytesSize = BYTE_SIZE_DIVISOR - writtenBytesRemainder
            val offsetBytes = ByteArray(offsetBytesSize)
            writeByteArray(offsetBytes)
        }
    }

    private fun writeToBuffer(onWrite: IByteBuffer.() -> Unit): Int {
        val oldPosition = buffer.position
        buffer.onWrite()
        return buffer.position - oldPosition
    }
}
