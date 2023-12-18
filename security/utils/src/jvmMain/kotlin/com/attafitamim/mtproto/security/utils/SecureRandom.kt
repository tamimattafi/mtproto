package com.attafitamim.mtproto.security.utils

import java.security.SecureRandom as PlatformRandom


actual class SecureRandom actual constructor(): ISecureRandom {

    private val random = PlatformRandom()

    override fun getRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        random.nextBytes(bytes)

        return bytes
    }
    override fun getRandomLong(): Long =
        random.nextLong()
}
