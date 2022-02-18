package com.attafitamim.mtproto.core.generator.parsers

import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import org.junit.jupiter.api.Test

class TestExample {
    @Test
    fun testMe() {
        val property = MTPropertyParser.parseProperty(
            "usersList:flags.0?Vector<User>",
            mapOf("T" to MTTypeSpec.Generic.Variable("T", MTTypeSpec.Primitive(Any::class)))
        )

        print(
            """
                -----------
                Test result
                $property
                -----------
            """.trimIndent()
        )
    }
}