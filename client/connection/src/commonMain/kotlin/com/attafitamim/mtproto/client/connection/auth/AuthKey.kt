
package com.attafitamim.mtproto.client.connection.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthKey(
    val key: ByteArray,
    val id: ByteArray,
    val gb: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AuthKey

        if (!key.contentEquals(other.key)) return false
        if (!id.contentEquals(other.id)) return false
        if (!gb.contentEquals(other.gb)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + id.contentHashCode()
        result = 31 * result + gb.contentHashCode()
        return result
    }
}