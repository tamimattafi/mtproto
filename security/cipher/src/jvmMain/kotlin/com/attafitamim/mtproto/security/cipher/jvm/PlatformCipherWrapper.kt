package com.attafitamim.mtproto.security.cipher.jvm

import com.attafitamim.mtproto.security.cipher.core.ICipher
import javax.crypto.Cipher

class PlatformCipherWrapper(
    override val platformCipher: Cipher
) : PlatformCipher(), ICipher {
    override fun updateData(data: ByteArray): ByteArray =
        platformCipher.update(data)

    override fun finalize(data: ByteArray): ByteArray =
        platformCipher.doFinal(data)
}
