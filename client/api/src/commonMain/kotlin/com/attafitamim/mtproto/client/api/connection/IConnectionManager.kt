package com.attafitamim.mtproto.client.api.connection

import com.attafitamim.mtproto.core.types.TLMethod

interface IConnectionManager {

    suspend fun initConnection(
        connectionType: ConnectionType = ConnectionType.Generic()
    )

    suspend fun <T : Any> sendRequest(
        method: TLMethod<T>,
        connectionType: ConnectionType = ConnectionType.Generic()
    ): T

    suspend fun release()
}
