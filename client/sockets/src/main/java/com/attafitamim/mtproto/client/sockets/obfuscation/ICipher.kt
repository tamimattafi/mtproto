package com.attafitamim.mtproto.client.sockets.obfuscation

interface ICipher {
    fun init(mode: CipherMode, key: ByteArray, iv: ByteArray)
    fun updateData(data: ByteArray): ByteArray

}
