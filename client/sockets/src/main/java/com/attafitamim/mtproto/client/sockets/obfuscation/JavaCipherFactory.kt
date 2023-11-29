package com.attafitamim.mtproto.client.sockets.obfuscation

class JavaCipherFactory : ICipherFactory {

    override fun createAESCTRCipher(): ICipher =
        JavaCipher(
            JavaCipher.ALGORITHM_CTR,
            JavaCipher.KEY_ALGORITHM_AES
        )
}
