package com.attafitamim.mtproto.buffer.core

interface IByteBuffer {
    var position: Int
    val remaining: Int

    fun rewind()

    fun putByte(byte: Byte)

    fun putByteArray(byteArray: ByteArray)

    fun putInt(value: Int)
    fun putShort(value: Short)

    fun putInt(position: Int, value: Int)

    fun putLong(value: Long)

    fun getByte(): Byte

    fun getByteArray(limit: Int): ByteArray

    fun getByteArray(): ByteArray

    fun fillByteArray(destination: ByteArray, offset: Int, limit: Int)

    fun getInt(): Int

    fun getLong(): Long

    fun order(byteOrder: ByteOrder)

    fun wrap(byteArray: ByteArray): IByteBuffer

    fun flip()
}