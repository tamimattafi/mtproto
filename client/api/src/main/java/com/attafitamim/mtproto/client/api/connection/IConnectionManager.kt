package com.attafitamim.mtproto.client.api.connection

import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject

interface IConnectionManager {

    @Throws(Exception::class)
    fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        flags: Int,
        connectionType: Int
    ): Int

    @Throws(Exception::class)
    fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        onQuickAck: IQuickAckDelegate?,
        flags: Int
    ): Int

    @Throws(Exception::class)
    fun <T : TLObject> sendRequest(
        method: TLMethod<T>,
        requestDelegate: IRequestDelegate<T>,
        onQuickAck: IQuickAckDelegate?,
        onWriteToSocket: IWriteToSocketDelegate?,
        flags: Int,
        datacenterId: Int,
        connectionType: Int,
        immediate: Boolean
    ): Int
    fun onAuthSuccess(userId: Int)
    fun cancelRequest(token: Int, notifyServer: Boolean)
    fun cleanup(resetKeys: Boolean)
    fun cancelRequestsForGuid(guid: Int)
    fun bindRequestToGuid(requestToken: Int, guid: Int)
    fun applyDatacenterAddress(datacenterId: Int, ipAddress: String?, port: Int)
    fun getConnectionState(): Int
    fun setUserId(id: Int)
    fun setPushConnectionEnabled(value: Boolean)
    fun switchBackend()
    fun getCurrentTimeMillis(): Long
    fun getCurrentTime(): Int
    fun getTimeDifference(): Int
    fun updateDcSettings()
    fun checkProxy(
        address: String?,
        port: Int,
        username: String?,
        password: String?,
        secret: String?,
        requestTimeDelegate: IRequestTimeDelegate?
    ): Long
    fun setAppPaused(isAppPaused: Boolean)
}
