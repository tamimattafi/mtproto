package com.attafitamim.mtproto.client.sockets.obfuscation

interface IObfuscator {
    fun init(input: ByteArray): ByteArray
    fun obfuscate(data: ByteArray): ByteArray
    fun clarify(data: ByteArray): ByteArray
}
