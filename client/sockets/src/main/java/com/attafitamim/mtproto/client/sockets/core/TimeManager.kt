package com.attafitamim.mtproto.client.sockets.core

class TimeManager(
    initialServerTime: Long = 0
) {

    private var serverTimeDelta = initialServerTime - localTime

    private val localTime: Long
        get() = System.currentTimeMillis()

    fun getServerTime() = localTime + serverTimeDelta

    fun setServerTime(serverTime: Long) {
        serverTimeDelta = serverTime - localTime
    }
}
