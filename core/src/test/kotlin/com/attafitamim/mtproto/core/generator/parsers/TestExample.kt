package com.attafitamim.mtproto.core.generator.parsers

import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import org.junit.jupiter.api.Test

class TestExample {
    @Test
    fun testMe() {
        val type = MTTypeParser.parseType(
            "help.config.Collection<test.List<[money.Stack<T>]>>",
            mapOf("T" to MTTypeSpec.Generic.Variable("T", MTTypeSpec.Primitive(Any::class)))
        )

        print(
            """
                -----------
                Test result
                $type
                -----------
            """.trimIndent()
        )
    }
}