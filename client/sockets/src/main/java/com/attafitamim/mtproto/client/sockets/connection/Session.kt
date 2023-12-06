package com.attafitamim.mtproto.client.sockets.connection

import com.attafitamim.mtproto.client.sockets.core.TimeManager
import com.attafitamim.mtproto.client.sockets.secure.RandomUtils
import com.attafitamim.mtproto.client.sockets.utils.generateMessageId

class Session(
    private val timeManager: TimeManager,
    val id: ByteArray = RandomUtils.randomSessionId()
) {

    @Volatile
    private var contentRelatedCount: Int = 0

    @Volatile
    private var lastMessageId: Long = 0

    fun generateSeqNo(contentRelated: Boolean): Int {
        val seqNo: Int

        synchronized(this) {
            seqNo = if (contentRelated) {
                val newSeqNo = contentRelatedCount * 2 + 1
                contentRelatedCount++

                newSeqNo
            } else {
                contentRelatedCount * 2
            }
        }

        return seqNo
    }

    fun generateMessageId(): Long {
        val weakMessageId = timeManager.generateMessageId()

        val newMessageId: Long
        synchronized(this) {
            newMessageId = weakMessageId.coerceAtLeast(lastMessageId + 4)
            lastMessageId = newMessageId
        }

        return newMessageId
    }
}