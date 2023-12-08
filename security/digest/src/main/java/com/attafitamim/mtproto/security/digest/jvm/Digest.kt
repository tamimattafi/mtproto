package com.attafitamim.mtproto.security.digest.jvm

import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.digest.core.IDigest
import com.attafitamim.mtproto.security.digest.core.IDigestFactory
import java.security.MessageDigest

class Digest(mode: DigestMode) : IDigest {

    private val messageDigest = MessageDigest.getInstance(mode.toJavaMode())

    override fun update(data: ByteArray) {
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

    companion object : IDigestFactory {
        private const val DIGEST_SHA_1 = "SHA-1"
        private const val DIGEST_SHA_256 = "SHA-256"

        override fun createDigest(mode: DigestMode): IDigest = Digest(mode)
    }
}
