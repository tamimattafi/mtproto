package com.attafitamim.mtproto.client.connection.utils

import com.attafitamim.mtproto.client.connection.session.Session
import kotlin.random.Random

const val MESSAGE_ID_STEP = 4
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

fun Session.createMessageId(): Long {
    // TODO: use server time instead of random
    val serverTime = Random.nextLong()
    val weakMessageId = convertTimeToMessageId(serverTime)
    val strongMessageId = lastMessageId + MESSAGE_ID_STEP
    val newMessageId = weakMessageId.coerceAtLeast(strongMessageId)
    lastMessageId = newMessageId
    return newMessageId
}

fun Session.getServerTime(): Long =
    System.currentTimeMillis() + serverTimeDiff
