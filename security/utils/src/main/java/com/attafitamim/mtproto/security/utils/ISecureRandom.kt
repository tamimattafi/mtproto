package com.attafitamim.mtproto.security.utils

interface ISecureRandom {
    fun getRandomBytes(size: Int): ByteArray
}
