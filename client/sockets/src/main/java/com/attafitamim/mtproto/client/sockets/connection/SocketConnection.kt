package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.client.sockets.core.socket.ISocket
import com.attafitamim.mtproto.client.sockets.core.socket.ISocketProvider
import com.attafitamim.mtproto.client.sockets.obfuscation.IObfuscator
import com.attafitamim.mtproto.client.sockets.obfuscation.toHex
import com.attafitamim.mtproto.client.sockets.utils.parseResponse
import com.attafitamim.mtproto.client.sockets.utils.serializeData
import com.attafitamim.mtproto.client.sockets.utils.toPublicMessage
import com.attafitamim.mtproto.core.types.TLMethod
import kotlinx.coroutines.flow.first

class SocketConnection(
    private val obfuscator: IObfuscator,
    private val socketProvider: ISocketProvider
) : IConnection {

    private val socket: ISocket by lazy {
        socketProvider.provideSocket()
    }

    override suspend fun connect() {
        socket.start()

        val initData = obfuscator.init()
        writePacket(initData)
    }

    override suspend fun <R : Any> sendRequest(request: TLMethod<R>): R {
        println("SOCKET_MESSAGE: sending request $request")

        val requestBytes = request.toPublicMessage()
        println("SOCKET_MESSAGE: sending message ${requestBytes.toHex()}")
        writeMessage(requestBytes)

        val obfuscatedBytes = readResponse()
        val clearBytes = obfuscator.clarify(obfuscatedBytes)
        println("SOCKET_MESSAGE: received ${clearBytes.toHex()}")

        val response = request.parseResponse(clearBytes)
        println("SOCKET_MESSAGE: received $response")
        return response
    }


    private fun writeMessage(message: ByteArray) {
        val packetBytes = serializeData {
            writeInt(message.size)
            writeByteArray(message)
        }

        println("SOCKET_MESSAGE: sending ${packetBytes.toHex()}")

        val obfuscatedBytes = obfuscator.obfuscate(packetBytes)
        writePacket(obfuscatedBytes)
    }

    private fun writePacket(packet: ByteArray) {
        println("SOCKET_PACKET: sent ${packet.toHex()}")
        socket.writeBytes(packet)
    }

    private suspend fun readResponse(): ByteArray {
        val response = socket.readBytes().first()
        println("SOCKET_PACKET: receiving ${response.toHex()}")

        return response
    }
 }
