package com.attafitamim.mtproto.security.cipher.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytesNoCopy
import platform.Foundation.getBytes

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    if (length.convert<Int>() == 0) return emptyArray

    return ByteArray(length.convert()).apply {
        usePinned {
            getBytes(it.addressOf(0), length)
        }
    }
}

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal fun <R> ByteArray.useNSData(block: (NSData) -> R): R {
    if (isEmpty()) return block(emptyNSData)

    return usePinned {
        block(
            NSData.dataWithBytesNoCopy(
                bytes = it.addressOf(0),
                length = size.convert()
            )
        )
    }
}

private val emptyNSData = NSData()
private val emptyArray = ByteArray(0)
