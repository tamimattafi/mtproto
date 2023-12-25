package com.attafitamim.mtproto.security.cipher.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytes
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFIndex
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRangeMake
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain

@OptIn(ExperimentalForeignApi::class)
internal inline fun CFMutableDictionary(
    size: CFIndex,
    block: CFMutableDictionaryRef?.() -> Unit
): CFMutableDictionaryRef? {
    val dictionary = CFDictionaryCreateMutable(
        null,
        size,
        null,
        null
    )

    dictionary.block()
    return dictionary
}

@OptIn(ExperimentalForeignApi::class)
internal fun CFMutableDictionaryRef?.add(key: CFTypeRef?, value: CFTypeRef?) {
    CFDictionaryAddValue(this, key, value)
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toCFData(): CFDataRef = CFDataCreate(
    null,
    toUByteArray().refTo(0),
    size.toLong()
)!!

@OptIn(ExperimentalForeignApi::class)
fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.convert()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Any?.retainBridgeAs(): T? = retainBridge()?.let { it as T }
@OptIn(ExperimentalForeignApi::class)
internal fun Any?.retainBridge(): CFTypeRef? = CFBridgingRetain(this)

@OptIn(ExperimentalForeignApi::class)
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> CFTypeRef?.releaseBridgeAs(): T? = releaseBridge()?.let { it as T }

@OptIn(ExperimentalForeignApi::class)
internal fun CFTypeRef?.releaseBridge(): Any? = CFBridgingRelease(this)

@OptIn(ExperimentalForeignApi::class)
internal fun CFTypeRef?.release(): Unit = CFRelease(this)

@OptIn(ExperimentalForeignApi::class)
internal inline fun <T : CFTypeRef?, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        releaseBridge()
    }
}