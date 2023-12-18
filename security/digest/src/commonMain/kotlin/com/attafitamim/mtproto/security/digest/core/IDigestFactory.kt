package com.attafitamim.mtproto.security.digest.core

interface IDigestFactory {
    fun createDigest(mode: DigestMode): IDigest
}
