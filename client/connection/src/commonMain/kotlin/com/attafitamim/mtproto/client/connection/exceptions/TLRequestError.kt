package com.attafitamim.mtproto.client.connection.exceptions

import com.attafitamim.mtproto.core.types.TLMethod

data class TLRequestError(
    val method: TLMethod<*>,
    val requestMessageId: Long,
    val authKeyId: String,
    val sessionId: Long,
    val code: Int,
    val text: String
) : Exception()
