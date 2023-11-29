package com.attafitamim.mtproto.client.sockets.test.helpers

import com.attafitamim.mtproto.client.sockets.buffer.CalculationByteBuffer
import com.attafitamim.mtproto.client.sockets.buffer.JavaByteBuffer
import com.attafitamim.mtproto.client.sockets.core.socket.ISocket
import com.attafitamim.mtproto.client.sockets.obfuscation.DefaultObfuscator
import com.attafitamim.mtproto.client.sockets.obfuscation.IObfuscator
import com.attafitamim.mtproto.client.sockets.obfuscation.ISecureRandom
import com.attafitamim.mtproto.client.sockets.obfuscation.JavaCipherFactory
import com.attafitamim.mtproto.client.sockets.obfuscation.toHex
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedInputStream
import com.attafitamim.mtproto.client.sockets.stream.TLBufferedOutputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import java.security.SecureRandom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Playground {

    private const val WEB_SOCKET_URL = "wss://localhost:2047/ws"
    private const val TCP_IP = "127.0.0.1"
    private const val TCP_PORT = 2045

    suspend fun <R : TLObject> sendRequest(method: TLMethod<R>): R {
        // Prepare socket factory
        val endpointProvider = ConnectionHelper.createdEndpointProvider(TCP_IP, TCP_PORT)
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)

        // Create Socket
        val socket = socketProvider.provideSocket()
        socket.start()

        ConnectionHelper.scope.launch {
            socket.readBytes().collect { bytes ->
                println("RECEIVED_BYTES: ${bytes.size}")
            }
        }

        // Serialize data to bytes
        val requestBytes = method.toPublicMessage()

        // Send request and get response
        socket.writeMessage(requestBytes)

        // Use bytes from response
        return method.parseResponse(ByteArray(1000))
    }

    private fun <R : Any> TLMethod<R>.parseResponse(responseBytes: ByteArray): R {
        val responseBuffer = JavaByteBuffer.wrap(responseBytes)
        val inputStream = TLBufferedInputStream(responseBuffer)

        val messageId = inputStream.readLong()
        val responseSize = inputStream.readInt()

        return parse(inputStream)
    }

    private fun <R : Any> TLMethod<R>.toPublicMessage(): ByteArray {
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

    private fun calculateData(onWrite: TLOutputStream.() -> Unit): Int {
        val calculationBuffer = CalculationByteBuffer()
        val calculationStream = TLBufferedOutputStream(calculationBuffer)
        calculationStream.onWrite()
        calculationBuffer.flip()

        return calculationBuffer.remaining
    }

    private fun serializeData(
        onWrite: TLOutputStream.() -> Unit
    ): ByteArray {
        val size = calculateData(onWrite)
        return serializeData(size, onWrite)
    }

    private fun serializeData(
        size: Int,
        onWrite: TLOutputStream.() -> Unit
    ): ByteArray {
        val javaBuffer = JavaByteBuffer.allocate(size)
        val outputStream = TLBufferedOutputStream(javaBuffer)
        outputStream.onWrite()
        javaBuffer.flip()

        return javaBuffer.getByteArray()
    }

    private fun generateMessageId() = System.currentTimeMillis()

    fun createSecureRandom(): ISecureRandom {
        val secureRandom = SecureRandom()

        return object : ISecureRandom {
            override fun getRandomBytes(size: Int): ByteArray {
                val bytes = ByteArray(size)
                secureRandom.nextBytes(bytes)

                return bytes
            }
        }
    }

    private fun createObfuscator(): IObfuscator {
        val secureRandom = createSecureRandom()
        val cipherFactory = JavaCipherFactory()

        return DefaultObfuscator(
            secureRandom,
            JavaByteBuffer.Companion,
            cipherFactory
        )
    }

    private suspend fun ISocket.writeMessage(request: ByteArray) {
        val obfuscator = createObfuscator()

        val initBytes = obfuscator.init(ByteArray(0))
        writeBytes(initBytes)
        println("FINAL_INIT: ${initBytes.toHex()}")

        delay(3000)


        // writeBytes(initBytes)
        delay(2000)

        val packetBytes = serializeData {
            writeWrappedByteArray(request, includePadding = false)
        }

        println("REQUEST: ${packetBytes.toHex()}")

        delay(1000)

        val obfuscatedBytes = obfuscator.obfuscate(packetBytes)
        println("OBFUSCATED: ${obfuscatedBytes.toHex()}")
        writeBytes(obfuscatedBytes)

        delay(10000)
    }
}