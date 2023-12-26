package com.attafitamim.mtproto.buffer.calculation

import com.attafitamim.mtproto.buffer.core.ByteOrder
import com.attafitamim.mtproto.buffer.core.IByteBuffer
import com.attafitamim.mtproto.buffer.utils.BYTE_SLOT_SIZE
import com.attafitamim.mtproto.buffer.utils.INT_SLOT_SIZE
import com.attafitamim.mtproto.buffer.utils.LONG_SLOT_SIZE
import com.attafitamim.mtproto.buffer.utils.SHORT_SLOT_SIZE
import kotlin.math.max

class CalculationByteBuffer(
    private var dataSize: Int = 0,
    override var position: Int = 0
) : IByteBuffer {

    override val remaining: Int
        get() = dataSize - position

    override fun rewind() {
        position = 0
    }

    override fun putByte(byte: Byte) {
        write(BYTE_SLOT_SIZE)
    }

    override fun putByteArray(byteArray: ByteArray) {
        write(byteArray.size)
    }

    override fun putInt(value: Int) {
        write(INT_SLOT_SIZE)
    }

    override fun putShort(value: Short) {
        write(SHORT_SLOT_SIZE)
    }

    override fun putLong(value: Long) {
        write(LONG_SLOT_SIZE)
    }

    override fun getByte(): Byte {
        read(BYTE_SLOT_SIZE)
        return 0
    }

    override fun getByteArray(limit: Int): ByteArray {
        read(limit)
        return ByteArray(limit)
    }

    override fun getByteArray(): ByteArray {
        read(remaining)
        return ByteArray(remaining)
    }

    override fun fillByteArray(destination: ByteArray, offset: Int, limit: Int) {
        read(limit)
    }

    override fun getShort(): Short {
        read(SHORT_SLOT_SIZE)
        return 0
    }

    override fun getInt(): Int {
        read(INT_SLOT_SIZE)
        return 0
    }

    override fun getLong(): Long {
        read(LONG_SLOT_SIZE)
        return 0
    }

    override fun order(byteOrder: ByteOrder) {
        // Do nothing, doesn't affect calculation
    }

    override fun flip() {
        position = 0
    }

    private fun read(length: Int) {
        position += length
    }

    private fun write(length: Int) {
        val remaining = remaining

        position += length
        dataSize += if (remaining <= 0) {
            length
        } else {
            max(remaining, length)
        }
    }
}
