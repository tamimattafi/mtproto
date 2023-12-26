package com.attafitamim.mtproto.sample

import com.attafitamim.mtproto.buffer.core.ByteOrder
import com.attafitamim.mtproto.buffer.core.IByteBuffer
import com.attafitamim.mtproto.client.sample.ConnectionHelper
import com.attafitamim.mtproto.client.sample.Playground
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.types.global.TLClientDHInnerData
import com.attafitamim.mtproto.serialization.stream.TLBufferedInputStream
import com.attafitamim.mtproto.serialization.stream.TLBufferedOutputStream
import com.attafitamim.mtproto.serialization.utils.calculateData
import com.attafitamim.mtproto.serialization.utils.serializeToBytes
import com.attafitamim.mtproto.serialization.utils.toTLInputStream
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer as PlatformByteBuffer

fun main() {

  /*  assureBufferWorksCorrectly()
    assureJavaBufferWorksCorrectly()*/
    val preferences = Preferences.userNodeForPackage(ConnectionHelper::class.java)
    val settings = PreferencesSettings(preferences)

    ConnectionHelper.scope.launch {
        Playground.initConnection(settings)
    }

    while (ConnectionHelper.scope.isActive) {
        Thread.sleep(10000)
    }
}

class JavaByteBuffer(
    private val byteBuffer: PlatformByteBuffer
) : IByteBuffer {

    override var position: Int
        get() = byteBuffer.position()
        set(value) {
            byteBuffer.position(value)
        }

    override val remaining: Int
        get() = byteBuffer.remaining()

    override fun rewind() {
        byteBuffer.rewind()
    }

    override fun putByte(byte: Byte) {
        byteBuffer.put(byte)
    }

    override fun putByteArray(byteArray: ByteArray) {
        byteBuffer.put(byteArray)
    }

    override fun putInt(value: Int) {
        byteBuffer.putInt(value)
    }

    override fun putShort(value: Short) {
        byteBuffer.putShort(value)
    }

    override fun putLong(value: Long) {
        byteBuffer.putLong(value)
    }

    override fun getByte(): Byte =
        byteBuffer.get()

    override fun getByteArray(limit: Int): ByteArray {
        val bytes = ByteArray(limit)
        byteBuffer.get(bytes)
        return bytes
    }

    override fun getByteArray(): ByteArray =
        getByteArray(remaining)

    override fun fillByteArray(destination: ByteArray, offset: Int, limit: Int) {
        byteBuffer[destination, offset, limit]
    }

    override fun getShort(): Short =
        byteBuffer.short

    override fun getInt(): Int =
        byteBuffer.int

    override fun getLong(): Long =
        byteBuffer.long

    override fun order(byteOrder: ByteOrder) {
        when (byteOrder) {
            ByteOrder.BIG_ENDIAN -> byteBuffer.order(java.nio.ByteOrder.BIG_ENDIAN)
            ByteOrder.LITTLE_ENDIAN -> byteBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        }
    }

    override fun flip() {
        byteBuffer.flip()
    }
}


fun assureBufferWorksCorrectly() {
    val nonce = TLInt128(byteArrayOf(1, 2, 3 , 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
    val serverNonce = TLInt128(byteArrayOf(1, 2, 3 , 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
    val retryId = 123L
    val gB = byteArrayOf(1, 2, 3, 4)

    val tlObject = TLClientDHInnerData.ClientDHInnerData(
        nonce,
        serverNonce,
        retryId,
        gB
    )

    println("SERIALIZED: $tlObject")

    val serializedTLObject = tlObject.serializeToBytes()
    println("BYTES: ${serializedTLObject.joinToString()}")

    val inputStream = serializedTLObject.toTLInputStream()
    val parsedTLObject = TLClientDHInnerData.parse(inputStream)
    println("PARSED: $parsedTLObject")

    assert(tlObject == parsedTLObject) {
        "Objects are not equal"
    }
}

fun assureJavaBufferWorksCorrectly() {
    val nonce = TLInt128(byteArrayOf(1, 2, 3 , 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
    val serverNonce = TLInt128(byteArrayOf(1, 2, 3 , 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
    val retryId = 123L
    val gB = byteArrayOf(1, 2, 3, 4)

    val tlObject = TLClientDHInnerData.ClientDHInnerData(
        nonce,
        serverNonce,
        retryId,
        gB
    )

    println("SERIALIZED: $tlObject")

    val size = calculateData {
        tlObject.serialize(this)
    }

    val outputBuffer = JavaByteBuffer(PlatformByteBuffer.allocate(size))
    val outputStream = TLBufferedOutputStream(outputBuffer)
    tlObject.serialize(outputStream)
    val serializedBytes = outputStream.toByteArray()
    println("BYTES: ${serializedBytes.joinToString()}")

    val inputBuffer = JavaByteBuffer(PlatformByteBuffer.wrap(serializedBytes))
    val inputStream = TLBufferedInputStream(inputBuffer)
    val parsedTLObject = TLClientDHInnerData.parse(inputStream)
    println("PARSED: $parsedTLObject")

    assert(tlObject == parsedTLObject) {
        "Objects are not equal"
    }
}
