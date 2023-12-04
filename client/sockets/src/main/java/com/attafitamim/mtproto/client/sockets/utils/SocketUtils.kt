package com.attafitamim.mtproto.client.sockets.utils

import com.attafitamim.mtproto.client.sockets.buffer.CalculationByteBuffer
import com.attafitamim.mtproto.client.sockets.buffer.JavaByteBuffer
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedInputStream
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedOutputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod


fun <R : Any> TLMethod<R>.parseResponse(responseBytes: ByteArray): R {
    val responseBuffer = JavaByteBuffer.wrap(responseBytes)
    val inputStream = TLBufferedInputStream(responseBuffer)

    val size = inputStream.readInt()
    println("RECEIVED_MESSAGE_SIZE: $size")

    val messageId = inputStream.readLong()
    println("RECEIVED_MESSAGE_ID: $messageId")

    val requestId = inputStream.readLong()
    println("RECEIVED_REQUEST_ID: $requestId")

    val responseSize = inputStream.readInt()
    println("RECEIVED_RESPONSE_SIZE: $responseSize")


    return parse(inputStream)
}

fun generateMessageId() = System.currentTimeMillis()

fun <R : Any> TLMethod<R>.toPublicMessage(): ByteArray {
    val authKeyId = 0L
    val requestMessageId = generateMessageId()
    val methodBytesSize = calculateData(::serialize)
    val methodBytes = serializeData(methodBytesSize, ::serialize)

    return serializeData {
        writeLong(authKeyId)
        writeLong(requestMessageId)
        writeInt(methodBytesSize)
        writeByteArray(methodBytes)
    }
}

fun calculateData(onWrite: TLOutputStream.() -> Unit): Int {
    val calculationBuffer = CalculationByteBuffer()
    val calculationStream = TLBufferedOutputStream(calculationBuffer)
    calculationStream.onWrite()
    calculationBuffer.flip()

    return calculationBuffer.remaining
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