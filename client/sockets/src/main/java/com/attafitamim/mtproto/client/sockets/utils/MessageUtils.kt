package com.attafitamim.mtproto.client.sockets.utils

import com.attafitamim.mtproto.client.sockets.core.TimeManager

const val TIME_SHIFT_POSITION = 32

fun TimeManager.synchronizeTimeWithMessageId(messageId: Int) {
    val serverTime = (messageId ushr TIME_SHIFT_POSITION) * SECOND_IN_MILLIS
    setServerTime(serverTime)
}

fun TimeManager.generateMessageId(): Long {
    val serverTime = getServerTime()
    return (serverTime / SECOND_IN_MILLIS) shl TIME_SHIFT_POSITION
}
