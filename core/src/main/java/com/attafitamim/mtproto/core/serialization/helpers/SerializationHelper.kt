package com.attafitamim.mtproto.core.serialization.helpers

import com.attafitamim.mtproto.core.serialization.streams.MTInputStream
import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream
import com.attafitamim.mtproto.core.types.MTObject
import java.io.InputStream
import kotlin.reflect.KClass

object SerializationHelper {
    fun <T : Any> serialize(stream: MTOutputStream, value: T) = stream.run {
        when(value) {
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
    }

    inline fun <reified T : Any> parse(stream: MTInputStream): T = stream.run {
        when(T::class) {
            Boolean::class -> readBoolean()
            Byte::class -> readByte()
            Int::class -> readInt()
            Long::class -> readLong()
            Double::class -> readDouble()
            String::class -> readString()
            ByteArray::class -> readByteArray()
            MTInputStream::class -> readInputStream()
            MTObject::class -> object : MTObject {
                override val constructorHash: Int
                    get() = TODO("Not yet implemented")

                override fun serialize(outputStream: MTOutputStream) {
                    TODO("Not yet implemented")
                }
            }
            else -> throw Exception()
        } as T
    }
}

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