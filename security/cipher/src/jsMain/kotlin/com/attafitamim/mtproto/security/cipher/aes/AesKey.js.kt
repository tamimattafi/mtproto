package com.attafitamim.mtproto.security.cipher.aes

actual class PlatformAesSecretKey(
    override val encodedBytes: ByteArray
) : AesSecretKey
