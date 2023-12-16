package com.attafitamim.mtproto.client.connection.core

sealed interface ConnectionEvent {

    data object Connected : ConnectionEvent

    data object Disconnected : ConnectionEvent

    data object ConnectionError : ConnectionEvent
}
