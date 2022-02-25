package com.attafitamim.mtproto.core.exceptions

import com.attafitamim.mtproto.core.types.MTObject
import java.lang.Exception
import kotlin.reflect.KClass

class MTObjectParseException(
    private val superClass: KClass<out MTObject>,
    private val hash: Int
) : Exception() {
    override val message: String
        get() = """
            Can't find a variant of ${superClass.simpleName}
            with the hash ${Integer.toHexString(hash)}
        """.trimIndent()
}