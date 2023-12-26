package com.attafitamim.mtproto.security.utils

import kotlin.random.Random

actual class SecureRandom actual constructor(): ISecureRandom {
    override fun getRandomBytes(size: Int): ByteArray = Random.nextBytes(size)
    override fun getRandomLong(): Long = Random.nextLong()
}
