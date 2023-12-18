package com.attafitamim.mtproto.core.exceptions

import com.attafitamim.mtproto.core.types.TLObject
import kotlin.reflect.KClass

class TLObjectParseException(
    val superClass: KClass<out TLObject>,
    val hash: Int
) : Exception() {
    @OptIn(ExperimentalStdlibApi::class)
    override val message: String
        get() = """
            Can't find a variant of ${superClass.simpleName}
            with the hash ${hash.toHexString(HexFormat.Default)}
        """.trimIndent()
}
