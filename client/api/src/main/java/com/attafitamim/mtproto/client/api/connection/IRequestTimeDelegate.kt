package com.attafitamim.mtproto.client.api.connection

fun interface IRequestTimeDelegate {
    fun run(time: Long)
}