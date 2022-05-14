package com.attafitamim.mtproto.core.serialization.helpers

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject
import kotlin.reflect.KClass

object SerializationHelper {
    fun <T : Any> serialize(stream: TLOutputStream, value: T) = stream.run {
        when(value) {
            is Boolean -> writeBoolean(value)
            is Byte -> writeByte(value)
            is Int -> writeInt(value)
            is Long -> writeLong(value)
            is Double -> writeDouble(value)
            is String -> writeString(value)
            is ByteArray -> writeByteArray(value)
            is TLInputStream -> writeInputStream(value)
            is TLObject -> value.serialize(this)
            else -> throw IllegalArgumentException("Unable to write value of type ${value::class.simpleName} to ${TLOutputStream::class.simpleName}")
        }
    }

    fun <T : Any> parse(stream: TLInputStream, clazz: KClass<T>): T = stream.run {
        when(clazz) {
            Boolean::class -> readBoolean()
            Byte::class -> readByte()
            Int::class -> readInt()
            Long::class -> readLong()
            Double::class -> readDouble()
            String::class -> readString()
            ByteArray::class -> readByteArray()
            TLInputStream::class -> readInputStream()
            TLObject::class -> object : TLObject {
                override val constructorHash: Int
                    get() = TODO("Not yet implemented")

                override fun serialize(outputStream: TLOutputStream) {
                    TODO("Not yet implemented")
                }
            }
            else -> throw Exception()
        } as T
    }
}

fun getTypeParseMethod(type: KClass<out Any>) = when(type) {
    Boolean::class -> TLInputStream::readBoolean
    Byte::class -> TLInputStream::readByte
    Int::class -> TLInputStream::readInt
    Long::class -> TLInputStream::readLong
    Double::class -> TLInputStream::readDouble
    String::class -> TLInputStream::readString
    ByteArray::class -> TLInputStream::readByteArray
    TLInputStream::class -> TLInputStream::readInputStream
    else -> throw Exception()
}

fun getTypeSerializeMethod(type: KClass<out Any>) = when(type) {
    Boolean::class -> TLOutputStream::writeBoolean
    Byte::class -> TLOutputStream::writeByte
    Int::class -> TLOutputStream::writeInt
    Long::class -> TLOutputStream::writeLong
    Double::class -> TLOutputStream::writeDouble
    String::class -> TLOutputStream::writeString
    ByteArray::class -> TLOutputStream::writeByteArray
    TLInputStream::class -> TLOutputStream::writeInputStream
    else -> throw Exception()
}
