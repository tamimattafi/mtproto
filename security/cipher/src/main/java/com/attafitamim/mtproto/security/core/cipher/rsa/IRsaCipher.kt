package com.attafitamim.mtproto.security.core.cipher.rsa

import com.attafitamim.mtproto.security.core.cipher.ICipher

interface IRsaCipher : ICipher {
    fun init(modulusHex: String, exponentHex: String)
}
