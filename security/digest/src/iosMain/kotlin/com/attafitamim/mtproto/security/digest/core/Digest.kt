package com.attafitamim.mtproto.security.digest.core

actual class Digest actual constructor(mode: DigestMode) : IDigest {

    private val platformDigest: BaseDigest = mode.toPlatform()

    override fun updateData(vararg data: ByteArray) {
        platformDigest.update(data)
    }

    override fun digest(vararg data: ByteArray): ByteArray =
        platformDigest.finalize(data)

    private fun DigestMode.toPlatform(): BaseDigest = when (this) {
        DigestMode.SHA1 -> SHA1Digest()
        DigestMode.SHA256 -> SHA256Digest()
    }
}
