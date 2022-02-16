package com.attafitamim.mtproto.core.exceptions

import com.attafitamim.mtproto.core.objects.MTObject
import java.lang.Exception

class MTObjectParseException(
    val superClass: Class<out MTObject>,
    val hash: Int
) : Exception() {
    override val message: String
        get() = """
            Can't find a variant of ${superClass.simpleName}
            with the hash ${Integer.toHexString(hash)}
        """.trimIndent()
}