package com.attafitamim.mtproto.security.digest.jvm

import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.digest.core.IDigest
import java.security.MessageDigest

class JvmDigest(mode: DigestMode) : IDigest {

    private val messageDigest = MessageDigest.getInstance(mode.toJavaMode())

    override fun updateData(data: ByteArray) {
        messageDigest.update(data)
    }

    override fun digest(data: ByteArray?): ByteArray =
        data?.let(messageDigest::digest) ?: messageDigest.digest()

    override fun reset() {
        messageDigest.reset()
    }

    private fun DigestMode.toJavaMode() = when (this) {
        DigestMode.SHA1 -> DIGEST_SHA_1
        DigestMode.SHA256 -> DIGEST_SHA_256
    }

    companion object {
        private const val DIGEST_SHA_1 = "SHA-1"
        private const val DIGEST_SHA_256 = "SHA-256"
    }
}
