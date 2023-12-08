package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.core.ICipher

interface IRsaCipher : ICipher {
    fun init(modulusHex: String, exponentHex: String)
}
