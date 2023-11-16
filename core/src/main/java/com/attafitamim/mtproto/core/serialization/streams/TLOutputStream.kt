package com.attafitamim.mtproto.core.serialization.streams

interface TLOutputStream {
    fun writeByte(value: Byte): Int
    fun writeInt(value: Int): Int
    fun writeLong(value: Long): Int
    fun writeDouble(value: Double): Int
    fun writeBoolean(value: Boolean): Int
    fun writeString(value: String): Int
    fun writeByteArray(value: ByteArray): Int
    fun writeInputStream(value: TLInputStream): Int
}
