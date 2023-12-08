package com.attafitamim.mtproto.security.core.cipher.aes

import com.attafitamim.mtproto.security.core.cipher.ICipher

interface IAesCipher : ICipher {
    fun init(key: ByteArray, iv: ByteArray)
}
