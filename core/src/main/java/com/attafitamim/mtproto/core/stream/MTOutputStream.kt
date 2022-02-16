package com.attafitamim.mtproto.core.stream

interface MTOutputStream {
    fun writeByte(value: Byte)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeDouble(value: Double)
    fun writeBoolean(value: Boolean)
    fun writeString(value: String)
    fun writeBytes(value: ByteArray, offset: Int = 0, count: Int = value.size)
    fun writeOutputBuffer(stream: MTInputStream)
}