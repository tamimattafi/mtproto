package com.attafitamim.mtproto.security.digest.core

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.CoreCrypto.CC_LONG
import platform.CoreCrypto.CC_SHA256_CTX
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA256_Final
import platform.CoreCrypto.CC_SHA256_Init
import platform.CoreCrypto.CC_SHA256_Update

@OptIn(ExperimentalForeignApi::class)
class SHA256Digest : BaseDigest() {

    override val digestSize: Int get() = CC_SHA256_DIGEST_LENGTH

    private val memScope = MemScope()
    private val context = memScope.alloc<CC_SHA256_CTX>()

    init {
        CC_SHA256_Init(c = context.ptr)
    }

    override fun update(data: CValuesRef<ByteVar>, dataLength: CC_LONG) {
        CC_SHA256_Update(
            c = context.ptr,
            data = data,
            len = dataLength
        )
    }

    override fun finalize(digest: CValuesRef<UByteVar>) {
        CC_SHA256_Final(
            md = digest,
            c = context.ptr
        )
    }
}

