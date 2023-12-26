package com.attafitamim.mtproto.security.digest.core

actual class Digest actual constructor(mode: DigestMode) : IDigest {

    override fun updateData(vararg data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun digest(vararg data: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }
}