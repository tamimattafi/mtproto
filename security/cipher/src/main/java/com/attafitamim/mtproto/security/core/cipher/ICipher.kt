package com.attafitamim.mtproto.security.core.cipher

interface ICipher {
    fun updateData(data: ByteArray): ByteArray
    fun finalize(data: ByteArray): ByteArray
}
