package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.api.connection.IQuickAckDelegate
import com.attafitamim.mtproto.client.api.connection.IRequestDelegate
import com.attafitamim.mtproto.client.api.connection.IRequestTimeDelegate
import com.attafitamim.mtproto.client.api.connection.IWriteToSocketDelegate
import com.attafitamim.mtproto.client.sockets.core.socket.ISocket
import com.attafitamim.mtproto.client.sockets.core.socket.ISocketProvider
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SocketConnectionManager(
    private val scope: CoroutineScope,
    private val socketProvider: ISocketProvider
) : IConnectionManager {

    val socket: ISocket = socketProvider.provideSocket()

    init {
        scope.launch {
            socket.readBytes().collectLatest { bytes ->
                println("SocketConnectionManager: bytes: $bytes")
            }
        }

        socket.start()
    }

    override fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        flags: Int,
        connectionType: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        onQuickAck: IQuickAckDelegate?,
        flags: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        onQuickAck: IQuickAckDelegate?,
        onWriteToSocket: IWriteToSocketDelegate?,
        flags: Int,
        datacenterId: Int,
        connectionType: Int,
        immediate: Boolean
    ): Int {
        TODO("Not yet implemented")
    }

    override fun onAuthSuccess(userId: Int) {
        TODO("Not yet implemented")
    }

    override fun cancelRequest(token: Int, notifyServer: Boolean) {
        TODO("Not yet implemented")
    }

    override fun cleanup(resetKeys: Boolean) {
        TODO("Not yet implemented")
    }

    override fun cancelRequestsForGuid(guid: Int) {
        TODO("Not yet implemented")
    }

    override fun bindRequestToGuid(requestToken: Int, guid: Int) {
        TODO("Not yet implemented")
    }

    override fun applyDatacenterAddress(datacenterId: Int, ipAddress: String?, port: Int) {
        TODO("Not yet implemented")
    }

    override fun getConnectionState(): Int {
        TODO("Not yet implemented")
    }

    override fun setUserId(id: Int) {
        TODO("Not yet implemented")
    }

    override fun setPushConnectionEnabled(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun switchBackend() {
        TODO("Not yet implemented")
    }

    override fun getCurrentTimeMillis(): Long {
        TODO("Not yet implemented")
    }

    override fun getCurrentTime(): Int {
        TODO("Not yet implemented")
    }

    override fun getTimeDifference(): Int {
        TODO("Not yet implemented")
    }

    override fun updateDcSettings() {
        TODO("Not yet implemented")
    }

    override fun checkProxy(
        address: String?,
        port: Int,
        username: String?,
        password: String?,
        secret: String?,
        requestTimeDelegate: IRequestTimeDelegate?
    ): Long {
        TODO("Not yet implemented")
    }

    override fun setAppPaused(isAppPaused: Boolean) {
        TODO("Not yet implemented")
    }
}
