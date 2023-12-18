package com.attafitamim.mtproto.client.connection.session

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: Long,
    val authKeyId: String,
    var contentRelatedCount: Int = 0,
    var lastMessageId: Long = 0,
    var serverTimeDiff: Long = 0
)