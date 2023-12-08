package com.attafitamim.mtproto.security.obfuscation

interface IObfuscator {
    fun init(): ByteArray
    fun obfuscate(data: ByteArray): ByteArray
    fun clarify(data: ByteArray): ByteArray
}
