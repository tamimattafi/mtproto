package com.attafitamim.mtproto.client.sockets.infrastructure.socket

sealed interface SocketEvent {

    data object Connected : SocketEvent

    sealed interface Error : SocketEvent {

        data class NoConnection(
            val cause: Throwable?,
            val retryCount: Int
        ) : Error

        data class MaxConnectRetriesReached(
            val retryCount: Int
        ) : Error
    }

    sealed interface Close : SocketEvent {

        data object Unspecified : Close

        data object Graceful : Close

        data class Abnormal(
            val errorCode: Short,
            val errorName: String,
            val message: String
        ) : Close
    }
}
