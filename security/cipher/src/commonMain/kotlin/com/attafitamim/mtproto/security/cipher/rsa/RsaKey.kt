package com.attafitamim.mtproto.security.cipher.rsa

sealed interface RsaKey {
    val fingerprint: Long
    val type: Type

    data class Raw(
        override val fingerprint: Long,
        override val type: Type,
        val modulusHex: String,
        val exponentHex: String
    ) : RsaKey

    enum class Type {
        PUBLIC,
        PRIVATE
    }
}
