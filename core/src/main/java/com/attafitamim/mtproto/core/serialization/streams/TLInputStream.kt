package com.attafitamim.mtproto.core.serialization.streams

interface TLInputStream {
    var position: Int
    fun readByte(): Byte
    fun readInt(): Int
    fun readLong(): Long
    fun readDouble(): Double
    fun readBoolean(): Boolean
    fun readString(): String
    fun readByteArray(): ByteArray
    fun readBytes(limit: Int): ByteArray
    fun readInputStream(): TLInputStream
    fun rewind()
    fun flip()
}
