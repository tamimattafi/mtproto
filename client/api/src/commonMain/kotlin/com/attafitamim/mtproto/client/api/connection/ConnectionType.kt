package com.attafitamim.mtproto.client.api.connection

sealed interface ConnectionType {

    data object Upload : ConnectionType

    data object Download : ConnectionType

    data class Generic(val key: String? = null): ConnectionType
}
