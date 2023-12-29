package com.attafitamim.mtproto.security.digest.core

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.CoreCrypto.CC_LONG
import platform.CoreCrypto.CC_MD5_CTX
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.CoreCrypto.CC_MD5_Final
import platform.CoreCrypto.CC_MD5_Init
import platform.CoreCrypto.CC_MD5_Update

@OptIn(ExperimentalForeignApi::class)
class MD5Digest : BaseDigest() {

    override val digestSize: Int get() = CC_MD5_DIGEST_LENGTH

    private val memScope = MemScope()
    private val context = memScope.alloc<CC_MD5_CTX>()

    init {
        CC_MD5_Init(c = context.ptr)
    }

    override fun update(data: CValuesRef<ByteVar>, dataLength: CC_LONG) {
        CC_MD5_Update(
            c = context.ptr,
            data = data,
            len = dataLength
        )
    }

    override fun finalize(digest: CValuesRef<UByteVar>) {
        CC_MD5_Final(
            md = digest,
            c = context.ptr
        )
    }
}

