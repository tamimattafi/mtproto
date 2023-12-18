package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.core.IConnectionProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocketProvider
import com.attafitamim.mtproto.security.obfuscation.DefaultObfuscator
import kotlinx.coroutines.CoroutineScope

class SocketConnectionProvider(
    private val scope: CoroutineScope,
    private val socketProvider: ISocketProvider
) : IConnectionProvider {

    override fun provideConnection(): IConnection {
        val obfuscator = DefaultObfuscator()
        val socket = socketProvider.provideSocket()
        return SocketConnection(
            scope,
            obfuscator,
            socket
        )
    }
}
