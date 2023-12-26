package com.attafitamim.mtproto.security.digest.core

import java.security.MessageDigest

actual class Digest actual constructor(mode: DigestMode) : IDigest {

    private val messageDigest = MessageDigest.getInstance(mode.toJavaMode())

    override fun updateData(vararg data: ByteArray) {
        update(data)
    }

    override fun digest(vararg data: ByteArray): ByteArray {
        update(data)
        return messageDigest.digest()
    }

    private fun update(data: Array<out ByteArray>) {
        data.forEach(messageDigest::update)
    }

    private fun DigestMode.toJavaMode() = when (this) {
        DigestMode.SHA1 -> DIGEST_SHA_1
        DigestMode.SHA256 -> DIGEST_SHA_256
    }

    private companion object {
        private const val DIGEST_SHA_1 = "SHA-1"
        private const val DIGEST_SHA_256 = "SHA-256"
    }
}
