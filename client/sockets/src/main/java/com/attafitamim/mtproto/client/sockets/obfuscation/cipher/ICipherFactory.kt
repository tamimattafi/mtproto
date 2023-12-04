package com.attafitamim.mtproto.client.sockets.obfuscation.cipher

interface ICipherFactory {
    fun createAESCTRCipher(): ICipher
}
