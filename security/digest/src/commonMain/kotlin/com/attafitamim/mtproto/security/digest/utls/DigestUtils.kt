package com.attafitamim.mtproto.security.digest.utls

import com.attafitamim.mtproto.security.digest.core.DigestMode
import com.attafitamim.mtproto.security.digest.core.IDigestFactory

fun IDigestFactory.sha1(vararg data: ByteArray): ByteArray =
    createDigest(DigestMode.SHA1)
    .apply {
        data.forEach(::updateData)
    }.digest()

fun IDigestFactory.sha256(vararg data: ByteArray): ByteArray =
    createDigest(DigestMode.SHA256)
    .apply {
        data.forEach(::updateData)
    }.digest()