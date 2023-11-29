package com.attafitamim.mtproto.sample.android

import com.attafitamim.mtproto.client.sockets.buffer.CalculationByteBuffer
import com.attafitamim.mtproto.client.sockets.buffer.JavaByteBuffer
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedInputStream
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedOutputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import java.nio.ByteBuffer

object Playground {



    fun <R : TLObject> sendRequest(method: TLMethod<R>): R {
        // Prepare socket factory
        val endpointProvider = ConnectionHelper.createdEndpointProvider()
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)

        // Create Socket
        val socket = socketProvider.provideSocket()
        socket.start()

        // Serialize data to bytes


        // Use bytes from response
        val responseBuffer = JavaByteBuffer.allocate(0)
        val inputStream = TLBufferedInputStream(responseBuffer)
        return method.parse(inputStream)
    }


    fun serializeRequest(method: TLMethod<out TLObject>): ByteArray {

        val calculationBuffer = CalculationByteBuffer()
        val calculationStream = TLBufferedOutputStream(calculationBuffer)
        method.serialize(calculationStream)

        val serializationBuffer = JavaByteBuffer.allocate(calculationBuffer.position)
        val serializationStream = TLBufferedOutputStream(serializationBuffer)
        method.serialize(serializationStream)
        serializationBuffer.rewind()

        return serializationBuffer.getByteArray()
    }
}