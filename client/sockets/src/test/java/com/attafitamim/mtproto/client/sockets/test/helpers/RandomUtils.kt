package com.attafitamim.mtproto.client.sockets.test.helpers

import java.security.SecureRandom

object RandomUtils {

    private val random = SecureRandom()

    fun randomByteArray(byteCount: Int): ByteArray {
        val byteArray = ByteArray(byteCount)
        random.nextBytes(byteArray)
        return byteArray
    }
}
