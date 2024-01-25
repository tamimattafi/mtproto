package com.attafitamim.mtproto.security.cipher.aes

actual class PlatformAesSecretKey(
    val platformKey: javax.crypto.SecretKey
) : AesSecretKey {

    override val encodedBytes: ByteArray
        get() = platformKey.encoded
}
