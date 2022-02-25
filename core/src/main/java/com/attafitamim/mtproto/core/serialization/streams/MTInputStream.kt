package com.attafitamim.mtproto.core.serialization.streams

interface MTInputStream {
    fun readByte(): Byte
    fun readInt(): Int
    fun readLong(): Long
    fun readDouble(): Double
    fun readBoolean(): Boolean
    fun readString(): String
    fun readByteArray(): ByteArray
    fun readInputStream(): MTInputStream
}