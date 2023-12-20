package com.attafitamim.mtproto.security.cipher.exception

class CryptographyException(
    message: String,
    cause: Exception? = null
) : Exception(message, cause)