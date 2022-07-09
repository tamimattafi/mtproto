package com.attafitamim.mtproto.client.tgnet

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream

fun interface UpdateHandler {
    fun onNewUpdate(inputStream: TLInputStream)
}