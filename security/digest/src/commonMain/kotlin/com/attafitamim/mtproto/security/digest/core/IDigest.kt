package com.attafitamim.mtproto.security.digest.core

interface IDigest {
    fun updateData(vararg data: ByteArray)
    fun digest(vararg data: ByteArray): ByteArray
    fun reset()
}
