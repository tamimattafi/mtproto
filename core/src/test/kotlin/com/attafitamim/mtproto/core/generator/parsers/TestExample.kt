package com.attafitamim.mtproto.core.generator.parsers

import org.junit.jupiter.api.Test

class TestExample {
    @Test
    fun testMe() {
        val mtObject = MTObjectParser.parseObject("core.invokeWithLayer#da9b0d0d {X:Type} layer:int query:!X = X;")
        print(
            """
                -----------
                Test result
                $mtObject
                -----------
            """.trimIndent()
        )
    }
}