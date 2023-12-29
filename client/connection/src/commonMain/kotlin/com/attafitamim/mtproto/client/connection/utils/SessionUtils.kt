package com.attafitamim.mtproto.client.connection.utils

import com.attafitamim.mtproto.client.connection.session.Session
import kotlin.random.Random
import kotlinx.datetime.Clock

const val CONTENT_RELATED_SEQ_STEP = 1
const val CONTENT_RELATED_SEQ_MULTIPLIER = 2

fun Session.generateSeqNo(contentRelated: Boolean): Int {
    val seqNo = if (contentRelated) {
        val newSeqNo = contentRelatedCount *
                CONTENT_RELATED_SEQ_MULTIPLIER +
                CONTENT_RELATED_SEQ_STEP

        contentRelatedCount++
        newSeqNo
    } else {
        contentRelatedCount * CONTENT_RELATED_SEQ_MULTIPLIER
    }

    return seqNo
}

fun createMessageId(): Long {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val randomInt = Random.nextInt()
    return currentTime + randomInt
}
