package com.attafitamim.mtproto.security.cipher.rsa

data class RsaKey(
    val fingerprint: Long,
    val modulusHex: String,
    val exponentHex: String
)
