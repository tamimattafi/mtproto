package com.attafitamim.mtproto.core.serialization.helpers

import com.attafitamim.mtproto.core.serialization.streams.MTInputStream
import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream
import com.attafitamim.mtproto.core.types.MTObject
import kotlin.reflect.KClass

fun getTypeParseMethod(type: KClass<out Any>) = when(type) {
    Boolean::class -> MTInputStream::readBoolean
    Byte::class -> MTInputStream::readByte
    Int::class -> MTInputStream::readInt
    Long::class -> MTInputStream::readLong
    Double::class -> MTInputStream::readDouble
    String::class -> MTInputStream::readString
    ByteArray::class -> MTInputStream::readByteArray
    MTInputStream::class -> MTInputStream::readInputStream
    else -> throw Exception()
}

fun getTypeSerializeMethod(type: KClass<out Any>) = when(type) {
    Boolean::class -> MTOutputStream::writeBoolean
    Byte::class -> MTOutputStream::writeByte
    Int::class -> MTOutputStream::writeInt
    Long::class -> MTOutputStream::writeLong
    Double::class -> MTOutputStream::writeDouble
    String::class -> MTOutputStream::writeString
    ByteArray::class -> MTOutputStream::writeByteArray
    MTInputStream::class -> MTOutputStream::writeInputStream
    else -> throw Exception()
}

inline fun <reified T : Any> MTOutputStream.writeValue(value: T) = when(value) {
    is Boolean -> writeBoolean(value)
    is Byte -> writeByte(value)
    is Int -> writeInt(value)
    is Long -> writeLong(value)
    is Double -> writeDouble(value)
    is String -> writeString(value)
    is ByteArray -> writeByteArray(value)
    is MTInputStream -> writeInputStream(value)
    is MTObject -> value.serialize(this)
    else -> throw IllegalArgumentException("Unable to write value of type ${value::class.simpleName} to ${MTOutputStream::class.simpleName}")
}