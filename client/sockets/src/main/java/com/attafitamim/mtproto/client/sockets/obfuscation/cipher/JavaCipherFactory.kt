package com.attafitamim.mtproto.client.sockets.obfuscation.cipher

class JavaCipherFactory : ICipherFactory {

    override fun createAESCTRCipher(): ICipher =
        AESCipher(
            AESCipher.ALGORITHM_AES_CTR,
            AESCipher.KEY_ALGORITHM_AES
        )
}
