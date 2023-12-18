package com.attafitamim.mtproto.security.cipher.aes

data class AesKey(
    val key: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AesKey

        if (!key.contentEquals(other.key)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
