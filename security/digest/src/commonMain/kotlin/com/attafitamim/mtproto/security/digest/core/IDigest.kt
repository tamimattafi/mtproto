package com.attafitamim.mtproto.security.digest.core

interface IDigest {
    fun updateData(data: ByteArray)
    fun digest(data: ByteArray? = null): ByteArray
    fun reset()
}
