package com.attafitamim.mtproto.serialization.utils

import com.attafitamim.mtproto.buffer.calculation.CalculationByteBuffer
import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.core.serialization.behavior.TLParser
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.stream.TLBufferedOutputStream

// Basic Constructors
const val BOOLEAN_CONSTRUCTOR_TRUE = 0x997275b5
const val BOOLEAN_CONSTRUCTOR_FALSE = 0xbc799737

// Limits
const val WRAPPED_BYTES_MAX_LENGTH = 254
const val BYTE_SIZE_DIVISOR = 4

// Bits
const val BYTE_BITS_COUNT = 8

fun calculateData(onWrite: TLOutputStream.() -> Unit): Int {
    val calculationBuffer = CalculationByteBuffer()
    val calculationStream = TLBufferedOutputStream(calculationBuffer)
    calculationStream.onWrite()
    calculationBuffer.flip()

    return calculationBuffer.remaining
}

fun TLSerializable.serializeToBytes(): ByteArray = serializeData {
    serialize(this)
}

fun serializeData(
    onWrite: TLOutputStream.() -> Unit
): ByteArray {
    val size = calculateData(onWrite)
    return serializeData(size, onWrite)
}

fun serializeData(
    size: Int,
    onWrite: TLOutputStream.() -> Unit
): ByteArray {
    val javaBuffer = JavaByteBuffer.allocate(size)
    val outputStream = TLBufferedOutputStream(javaBuffer)
    outputStream.onWrite()
    javaBuffer.flip()

    return javaBuffer.getByteArray()
}

fun ByteArray.toTLInputStream(): TLInputStream {
    val javaBuffer = JavaByteBuffer.wrap(this)
    return TLBufferedInputStream(javaBuffer)
}

fun <T : Any> TLParser<T>.parseBytes(byteArray: ByteArray) =
    parse(byteArray.toTLInputStream())

fun <T : Any> TLInputStream.tryParse(onParse: (TLInputStream) -> T): T? {
    val result = kotlin.runCatching {
        onParse.invoke(this)
    }

    return if (result.isSuccess) {
        result.getOrThrow()
    } else {
        rewind()
        null
    }
}
