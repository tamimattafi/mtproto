package com.attafitamim.mtproto.client.sockets.obfuscation

interface ICipherFactory {
    fun createAESCTRCipher(): ICipher
}
