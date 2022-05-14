package com.attafitamim.mtproto.core.exceptions

import com.attafitamim.mtproto.core.types.TLObject
import java.lang.Exception
import kotlin.reflect.KClass

class TLObjectParseException(
    private val superClass: KClass<out TLObject>,
    private val hash: Int
) : Exception() {
    override val message: String
        get() = """
            Can't find a variant of ${superClass.simpleName}
            with the hash ${Integer.toHexString(hash)}
        """.trimIndent()
}
