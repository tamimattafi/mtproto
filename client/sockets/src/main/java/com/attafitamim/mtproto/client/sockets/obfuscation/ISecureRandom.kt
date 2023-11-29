package com.attafitamim.mtproto.client.sockets.obfuscation

interface ISecureRandom {
    fun getRandomBytes(size: Int): ByteArray
}