package com.attafitamim.mtproto.security.cipher.aes

import com.attafitamim.mtproto.security.cipher.core.ICipher

interface IAesCipher : ICipher {
    fun init(key: ByteArray, iv: ByteArray)
}
