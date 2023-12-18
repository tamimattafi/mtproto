package com.attafitamim.mtproto.security.obfuscation

interface IObfuscator {
    suspend fun init(): ByteArray
    suspend fun obfuscate(data: ByteArray): ByteArray
    suspend fun clarify(data: ByteArray): ByteArray
    suspend fun release()
    suspend fun isInitialized(): Boolean
}
