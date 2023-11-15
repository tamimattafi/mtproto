package com.attafitamim.mtproto.client.sockets.core.socket

sealed interface SocketEvent {

    object Open : SocketEvent

    data class OpenError(
        val retryCount: Int,
        val throwable: Throwable?
    ) : SocketEvent

    data class Close(
        val type: Type,
        val message: String?
    ) : SocketEvent {

        enum class Type {
            GRACEFUL,
            ABNORMAL
        }
    }
}
