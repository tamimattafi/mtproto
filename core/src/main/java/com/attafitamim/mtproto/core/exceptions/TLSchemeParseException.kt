package com.attafitamim.mtproto.core.exceptions

import org.gradle.api.GradleException

internal class TLSchemeParseException(
    private val rawScheme: String,
    private val reason: String
) : GradleException() {
    override val message: String
        get() = """
            Error parsing scheme:
            Raw Scheme: $rawScheme 
            Reason: $reason
        """.trimIndent()
}
