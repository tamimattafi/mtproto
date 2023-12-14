package com.attafitamim.mtproto.client.connection.utils

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.utils.calculateData
import com.attafitamim.mtproto.serialization.utils.serializeData


fun <R : Any> TLMethod<R>.parseResponse(responseBytes: ByteArray): R {
    val responseBuffer = JavaByteBuffer.wrap(responseBytes)
    val inputStream = TLBufferedInputStream(responseBuffer)

    com.attafitamim.mtproto.client.scheme.containers.global.TLPublicMessage
    val messageId = inputStream.readLong()
    val requestId = inputStream.readLong()
    val responseSize = inputStream.readInt()
    return parse(inputStream)
}

fun <R : Any> TLMethod<R>.toPublicMessage(
    requestMessageId: Long
): ByteArray {
    val authKeyId = 0L
    val methodBytesSize = calculateData(::serialize)
    val methodBytes = serializeData(methodBytesSize, ::serialize)

    return serializeData {
        writeLong(authKeyId)
        writeLong(requestMessageId)
        writeInt(methodBytesSize)
        writeByteArray(methodBytes)
    }
}
