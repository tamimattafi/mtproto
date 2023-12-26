package com.attafitamim.mtproto.buffer.core

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

    override fun putShort(value: Short) {
        putNumber(value.toLong(), SHORT_SLOT_SIZE)
    }

    override fun putInt(value: Int) {
        putNumber(value.toLong(), INT_SLOT_SIZE)
    }

    override fun putLong(value: Long) {
        putNumber(value, LONG_SLOT_SIZE)
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
        val lastIndex = position + limit
        bufferData.copyInto(destination, offset, position, lastIndex)
        position += limit
    }

    override fun getShort(): Short =
        getNumber(SHORT_SLOT_SIZE).toShort()

    override fun getInt(): Int =
        getNumber(INT_SLOT_SIZE).toInt()

    override fun getLong(): Long =
        getNumber(LONG_SLOT_SIZE)

    override fun order(byteOrder: ByteOrder) {
        this.order = byteOrder
    }

    override fun flip() {
        limit = position
        position = 0
    }

    private fun putNumber(value: Long, slots: Int) {
        val lastIndex = slots - 1

        for (bytePosition in 0 .. lastIndex) {
            val bitIndex = when (order) {
                ByteOrder.BIG_ENDIAN -> lastIndex - bytePosition
                ByteOrder.LITTLE_ENDIAN -> bytePosition
            }

            val bitPosition = bitIndex * BYTE_BITS_COUNT
            bufferData[position + bytePosition] = (value ushr bitPosition).toByte()
        }

        position += slots
    }

    private fun getNumber(slots: Int): Long {
        val longBytes = getByteArray(slots)

        var value = 0L
        for (bytePosition in 0 .. longBytes.lastIndex) {
            val bitIndex = when (order) {
                ByteOrder.BIG_ENDIAN -> longBytes.lastIndex - bytePosition
                ByteOrder.LITTLE_ENDIAN -> bytePosition
            }

            val bitPosition = bitIndex * BYTE_BITS_COUNT
            val longValue = longBytes[bytePosition].toLong()
            value += (longValue and 0xFF) shl bitPosition
        }

        return value
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