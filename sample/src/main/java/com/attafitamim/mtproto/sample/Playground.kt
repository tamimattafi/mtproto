package com.attafitamim.mtproto.sample

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.scheme.methods.global.TLGetFutureSalts
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import java.math.BigInteger
import kotlinx.coroutines.delay

object Playground {

    private const val WEB_SOCKET_URL =
    private const val SERVER_IP = "127.0.0.1"
    private const val SERVER_PORT = 2047

    suspend fun initConnection() {
        val endpointProvider = ConnectionHelper.createdEndpointProvider(WEB_SOCKET_URL)
        val socketProvider = ConnectionHelper.createSocketProvider(endpointProvider)
        val connectionProvider = ConnectionHelper.createConnectionProvider(socketProvider)

        val passport = ConnectionPassport(
            apiId =,
            apiHash =,
            deviceModel = "android",
            systemVersion = "1.2.3",
            appVersion = "playground-1",
            systemLangCode = "en",
            langPack = "en",
            langCode = "en",
            layer = 130
        )

        val connectionManager = ConnectionHelper.createConnectionManager(
            connectionProvider,
            passport
        )

        // Public request
        val getSalts = TLGetFutureSalts(1)
        val connectionType = ConnectionType.Generic("calls")
        val salts = connectionManager.sendRequest(getSalts, connectionType)
        println("TLGetFutureSalts: $salts")

        delay(3000)
    }


    fun readUInt(src: ByteArray, offset: Int): Long {
        val a = (src[offset].toInt() and 0xFF).toLong()
        val b = (src[offset + 1].toInt() and 0xFF).toLong()
        val c = (src[offset + 2].toInt() and 0xFF).toLong()
        val d = (src[offset + 3].toInt() and 0xFF).toLong()
        return a + (b shl 8) + (c shl 16) + (d shl 24)
    }

    fun readLong(src: ByteArray, offset: Int): Long {
        val a: Long = readUInt(src, offset)
        val b: Long = readUInt(src, offset + 4)
        return (a and 0xFFFFFFFFL) + (b and 0xFFFFFFFFL shl 32)
    }

    private fun fromBigInt(value: BigInteger): ByteArray {
        val res = value.toByteArray()
        return if (res[0].toInt() == 0) {
            val res2 = ByteArray(res.size - 1)
            System.arraycopy(res, 1, res2, 0, res2.size)
            res2
        } else {
            res
        }
    }

    private fun <T : Any> TLVector<T>.toList() = when (this) {
        is TLVector.Vector -> elements
    }
}