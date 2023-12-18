package com.attafitamim.mtproto.core.serialization.streams

interface TLOutputStream {
    var position: Int
    fun writeByte(value: Byte): Int
    fun writeInt(value: Int): Int
    fun writeIntAsBytes(value: Int, limit: Int): Int
    fun writeLong(value: Long): Int
    fun writeDouble(value: Double): Int
    fun writeBoolean(value: Boolean): Int
    fun writeString(value: String): Int
    fun writeByteArray(value: ByteArray): Int
    fun writeWrappedByteArray(value: ByteArray, appendPadding: Boolean = false): Int
    fun writeInputStream(value: TLInputStream): Int
    fun rewind()
    fun flip()
    fun toByteArray(clip: Boolean = true): ByteArray
}
