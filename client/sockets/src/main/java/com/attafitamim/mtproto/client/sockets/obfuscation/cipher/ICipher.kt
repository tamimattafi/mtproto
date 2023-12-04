package com.attafitamim.mtproto.client.sockets.obfuscation.cipher

interface ICipher {
    fun init(mode: CipherMode, key: ByteArray, iv: ByteArray)
    fun updateData(data: ByteArray): ByteArray
}
