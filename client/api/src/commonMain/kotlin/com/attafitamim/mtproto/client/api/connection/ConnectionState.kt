package com.attafitamim.mtproto.client.api.connection

sealed interface ConnectionState {
    data object Connecting : ConnectionState

    data object Connected : ConnectionState

    data object Disconnected : ConnectionState
}
