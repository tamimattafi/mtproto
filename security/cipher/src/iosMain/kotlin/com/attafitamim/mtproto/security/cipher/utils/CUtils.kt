package com.attafitamim.mtproto.security.cipher.utils

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo

private val almostEmptyArray = ByteArray(1)

//this hack will be dropped with introducing of new IO or functions APIs
internal fun ByteArray.fixEmpty(): ByteArray = if (isNotEmpty()) this else almostEmptyArray

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.refToFixed(index: Int): CValuesRef<ByteVar> {
    if (index == size) return almostEmptyArray.refTo(0)
    return refTo(index)
}
