package com.attafitamim.mtproto.security.utils

actual class SecureRandom actual constructor(): ISecureRandom {

    override fun getRandomBytes(size: Int): ByteArray = TODO()
    override fun getRandomLong(): Long = TODO()
}
