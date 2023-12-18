package com.attafitamim.mtproto.security.digest.core

actual object Digest : IDigestFactory {

    override fun createDigest(mode: DigestMode): IDigest =
        TODO()
}
