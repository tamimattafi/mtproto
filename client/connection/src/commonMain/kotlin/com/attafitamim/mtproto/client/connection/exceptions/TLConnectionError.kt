package com.attafitamim.mtproto.client.connection.exceptions

data class TLConnectionError(
    override val message: String?
) : Exception(message)
