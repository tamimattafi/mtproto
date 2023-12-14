package com.attafitamim.mtproto.client.api.connection

import com.attafitamim.mtproto.core.types.TLMethod

interface IConnectionManager {

    suspend fun <T : Any> sendRequest(
        method: TLMethod<T>,
        connectionType: ConnectionType
    ): T

    suspend fun release()
}
