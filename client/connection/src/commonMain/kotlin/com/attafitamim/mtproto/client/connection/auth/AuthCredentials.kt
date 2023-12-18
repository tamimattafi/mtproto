package com.attafitamim.mtproto.client.connection.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthCredentials(
    val key: AuthKey,
    val keyId: ByteArray,
    var serverSalt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AuthCredentials

        if (key != other.key) return false
        if (!keyId.contentEquals(other.keyId)) return false
        if (serverSalt != other.serverSalt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + keyId.contentHashCode()
        result = 31 * result + serverSalt.hashCode()
        return result
    }
}