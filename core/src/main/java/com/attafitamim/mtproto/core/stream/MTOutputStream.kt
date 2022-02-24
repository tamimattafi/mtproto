package com.attafitamim.mtproto.core.stream

interface MTOutputStream {
    fun writeByte(value: Byte)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeDouble(value: Double)
    fun writeBoolean(value: Boolean)
    fun writeString(value: String)
    fun writeByteArray(value: ByteArray)
    fun writeInputBuffer(value: MTInputStream)
}