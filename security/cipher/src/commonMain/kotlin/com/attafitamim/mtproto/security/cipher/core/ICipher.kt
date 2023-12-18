package com.attafitamim.mtproto.security.cipher.core

interface ICipher {
    fun updateData(data: ByteArray): ByteArray
    fun finalize(data: ByteArray): ByteArray

    companion object
}
