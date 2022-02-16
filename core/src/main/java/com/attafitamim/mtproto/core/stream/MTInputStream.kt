package com.attafitamim.mtproto.core.stream

interface MTInputStream {
    fun readByte(): Byte
    fun readInt(): Int
    fun readLong(): Long
    fun readDouble(): Double
    fun readBoolean(): Boolean
    fun readString(): String
    fun readBytes(output: ByteArray)
    fun readBytes(count: Int): ByteArray
    fun readByteArray(): ByteArray
    fun readOutputBuffer(): MTInputStream
}