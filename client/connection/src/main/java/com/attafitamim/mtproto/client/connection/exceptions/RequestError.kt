package com.attafitamim.mtproto.client.connection.exceptions

data class RequestError(
    val code: Int,
    val text: String
) : Exception()
