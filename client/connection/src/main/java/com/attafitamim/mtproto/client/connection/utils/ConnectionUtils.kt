package com.attafitamim.mtproto.client.connection.utils

const val TIME_SHIFT_POSITION = 32
fun convertTimeToMessageId(serverTime: Long): Long =
    (serverTime / SECOND_IN_MILLIS) shl TIME_SHIFT_POSITION
