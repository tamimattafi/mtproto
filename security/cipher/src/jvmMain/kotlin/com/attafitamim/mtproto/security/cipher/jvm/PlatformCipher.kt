package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.core.ICipher
import javax.crypto.Cipher

abstract class PlatformCipher : ICipher {
    abstract val platformCipher: Cipher
}