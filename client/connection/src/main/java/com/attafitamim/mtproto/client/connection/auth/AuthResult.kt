
package com.attafitamim.mtproto.client.connection.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val credentials: AuthCredentials,
    val serverTime: Long
)
