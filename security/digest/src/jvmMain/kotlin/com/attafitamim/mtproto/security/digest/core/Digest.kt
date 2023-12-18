package com.attafitamim.mtproto.security.digest.core

import com.attafitamim.mtproto.security.digest.jvm.JvmDigest

actual object Digest : IDigestFactory {

    override fun createDigest(mode: DigestMode): IDigest =
        JvmDigest(mode)
}
