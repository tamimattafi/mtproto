package com.attafitamim.mtproto.client.sockets.stream

import com.attafitamim.mtproto.client.sockets.buffer.ByteOrder
import com.attafitamim.mtproto.client.sockets.buffer.IByteBuffer
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_FALSE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BOOLEAN_CONSTRUCTOR_TRUE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_SIZE_DIVISOR
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.BYTE_SLOT_SIZE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.INT_SLOT_SIZE
import com.attafitamim.mtproto.client.sockets.serialization.SerializationUtils.WRAPPED_BYTES_MAX_LENGTH
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

class TLBufferedInputStream(
    private val buffer: IByteBuffer
) : TLInputStream {

    override fun readByte(): Byte =
        buffer.getByte()

    override fun readInt(): Int = with(buffer) {
        order(ByteOrder.LITTLE_ENDIAN)
        val value = getInt()
        order(ByteOrder.BIG_ENDIAN)
        return value
    }

    override fun readLong(): Long = with(buffer) {
        order(ByteOrder.LITTLE_ENDIAN)
        val value = getLong()
        order(ByteOrder.BIG_ENDIAN)
        return value
    }

    override fun readDouble(): Double {
        val longBits = readLong()
        return Double.fromBits(longBits)
    }

    override fun readBoolean(): Boolean {
        return when (val constructor = readInt()) {
            BOOLEAN_CONSTRUCTOR_TRUE.toInt() -> true
            BOOLEAN_CONSTRUCTOR_FALSE.toInt() -> false
            else -> error("unknown boolean with constructor $constructor")
        }
    }

    override fun readString(): String {
        val stringBytes = readWrappedBytes()
        return stringBytes.decodeToString()
    }

    override fun readByteArray(): ByteArray =
        buffer.getByteArray()

    override fun readBytes(limit: Int): ByteArray =
        buffer.getByteArray(limit)

    override fun readInputStream(): TLInputStream =
        TLBufferedInputStream(buffer)

    private fun readIntFromBytes(
        limit: Int = BYTE_SLOT_SIZE
    ): Int {
        val values = ByteArray(INT_SLOT_SIZE).apply {
            fill(0, limit, lastIndex)
        }

        buffer.fillByteArray(values, offset = 0, limit = limit)

        val valueBuffer = buffer.wrap(values).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }

        return valueBuffer.getInt()
    }

    /**
     * @see <a href="https://core.telegram.org/mtproto/serialize#base-types">Base Types</a>
     */
    private fun readWrappedBytes(): ByteArray {
        var readBytesSize = 0
        var length = readIntFromBytes(limit = BYTE_SLOT_SIZE)
        readBytesSize += BYTE_SLOT_SIZE

        if (length >= WRAPPED_BYTES_MAX_LENGTH) {
            val int24Slots = INT_SLOT_SIZE - 1
            length = readIntFromBytes(limit = int24Slots)
            readBytesSize += int24Slots
        }

        val bytes = readBytes(length)
        readBytesSize += length

        val readBytesRemainder = readBytesSize % BYTE_SIZE_DIVISOR
        if (readBytesRemainder > 0) {
            val offsetBytesSize = BYTE_SIZE_DIVISOR - readBytesRemainder
            buffer.position += offsetBytesSize
        }

        return bytes
    }
}