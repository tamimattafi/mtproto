package com.attafitamim.mtproto.security.cipher.aes

interface AesSecretKey {
    val encodedBytes: ByteArray
}

expect class PlatformAesSecretKey : AesSecretKey

data class EncodedAesSecretKey(
    override val encodedBytes: ByteArray
) : AesSecretKey {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EncodedAesSecretKey

        if (!encodedBytes.contentEquals(other.encodedBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return encodedBytes.contentHashCode()
    }
}

data class AesKey(
    val key: AesSecretKey,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AesKey

        if (key != other.key) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
