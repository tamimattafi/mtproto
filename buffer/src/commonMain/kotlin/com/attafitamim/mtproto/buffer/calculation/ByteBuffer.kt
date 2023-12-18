package com.attafitamim.mtproto.buffer.calculation

import com.attafitamim.mtproto.buffer.core.ByteOrder
import com.attafitamim.mtproto.buffer.core.IByteBuffer
import com.attafitamim.mtproto.buffer.core.IByteBufferProvider
import com.attafitamim.mtproto.buffer.utils.BYTE_BITS_COUNT
import com.attafitamim.mtproto.buffer.utils.INT_SLOT_SIZE
import com.attafitamim.mtproto.buffer.utils.LONG_SLOT_SIZE
import com.attafitamim.mtproto.buffer.utils.SHORT_SLOT_SIZE

class ByteBuffer(
    val bufferData: ByteArray,
    var order: ByteOrder = ByteOrder.BIG_ENDIAN
) : IByteBuffer {

    override var position: Int = 0
    private var limit: Int = bufferData.size

    override val remaining: Int
        get() = limit - position

    override fun rewind() {
        position = 0
    }

    override fun putByte(byte: Byte) {
        bufferData[position] = byte
        position++
    }

    override fun putByteArray(byteArray: ByteArray) {
        byteArray.copyInto(bufferData, position)
        position += byteArray.size
    }

    override fun putInt(value: Int) {
        val lastIndex = INT_SLOT_SIZE - 1

        for (bytePosition in 0 .. lastIndex) {
            val bitPosition = (lastIndex - bytePosition) * BYTE_BITS_COUNT
            bufferData[position + bytePosition] = (value ushr bitPosition).toByte()
        }

        position += INT_SLOT_SIZE
    }

    override fun putShort(value: Short) {
        val lastIndex = SHORT_SLOT_SIZE - 1

        val intValue = value.toInt()
        for (bytePosition in 0 .. lastIndex) {
            val bitPosition = (lastIndex - bytePosition) * BYTE_BITS_COUNT
            bufferData[position + bytePosition] = (intValue ushr bitPosition).toByte()
        }

        position += SHORT_SLOT_SIZE
    }

    override fun putLong(value: Long) {
        val lastIndex = LONG_SLOT_SIZE - 1

        for (bytePosition in 0 .. lastIndex) {
            val bitPosition = (lastIndex - bytePosition) * BYTE_BITS_COUNT
            bufferData[position + bytePosition] = (value ushr bitPosition).toByte()
        }

        position += LONG_SLOT_SIZE
    }

    override fun getByte(): Byte {
        val byte = bufferData[position]
        position++
        return byte
    }

    override fun getByteArray(limit: Int): ByteArray {
        val lastIndex = position + limit - 1
        val bytes = bufferData.sliceArray(position..lastIndex)
        position += limit
        return bytes
    }

    override fun getByteArray(): ByteArray =
        getByteArray(remaining)

    override fun fillByteArray(destination: ByteArray, offset: Int, limit: Int) {
        val lastIndex = position + limit - 1
        bufferData.copyInto(destination, offset, position, lastIndex)
        position += limit
    }

    override fun getInt(): Int {
        val intBytes = getByteArray(INT_SLOT_SIZE)
        var value = 0
        for (bytePosition in 0 .. intBytes.lastIndex) {
            val bitPosition = (intBytes.lastIndex - bytePosition) * BYTE_BITS_COUNT
            val intValue = intBytes[bytePosition].toInt()
            value += (intValue and 0xFF) shl bitPosition
        }

        return value
    }

    override fun getLong(): Long {
        val longBytes = getByteArray(LONG_SLOT_SIZE)

        var value = 0L
        for (longPosition in 0 .. longBytes.lastIndex) {
            val bitPosition = (longBytes.lastIndex - longPosition) * BYTE_BITS_COUNT
            val longValue = longBytes[longPosition].toLong()
            value += (longValue and 0xFF) shl bitPosition
        }

        return value
    }

    override fun order(byteOrder: ByteOrder) {
        this.order = byteOrder
    }

    override fun flip() {
        limit = position
        position = 0
    }

    companion object : IByteBufferProvider {

        override fun allocate(capacity: Int): IByteBuffer {
            val bufferData = ByteArray(capacity)
            return wrap(bufferData)
        }

        override fun wrap(byteArray: ByteArray): IByteBuffer =
            ByteBuffer(byteArray)
    }
}