package com.attafitamim.mtproto.security.digest.core

import com.attafitamim.mtproto.security.utils.fixEmpty
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.CoreCrypto.CC_LONG

@OptIn(ExperimentalForeignApi::class)
abstract class BaseDigest {

    abstract val digestSize: Int

    protected abstract fun update(data: CValuesRef<ByteVar>, dataLength: CC_LONG)
    protected abstract fun finalize(digest: CValuesRef<UByteVar>)

    fun finalize(data: Array<out ByteArray>): ByteArray {
        update(data)

        val output = ByteArray(digestSize)
        finalize(output.asUByteArray().refTo(0))
        return output
    }

    fun update(data: Array<out ByteArray>) {
        data.forEach(::update)
    }

    private fun update(data: ByteArray) {
        update(
            data.fixEmpty().refTo(0),
            data.size.convert()
        )
    }
}
