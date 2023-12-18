package com.attafitamim.mtproto.core.serialization.helpers

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject

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
            is TLContainer -> value.serialize(this)
            is TLMethod<*> -> value.serialize(this)
            else -> throw IllegalArgumentException("Unable to write value of type ${value::class.simpleName} to ${TLOutputStream::class.simpleName}")
        }
    }
}
