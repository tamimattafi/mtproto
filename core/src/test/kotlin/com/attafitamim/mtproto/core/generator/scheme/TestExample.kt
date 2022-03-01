package com.attafitamim.mtproto.core.generator.scheme

import com.attafitamim.mtproto.core.generator.poet.generation.MTGenerator
import org.junit.jupiter.api.Test

class TestExample {

    @Test
    fun testMe() {
        var schemeFilesDir: String = "/Users/tamimattafi/Projects/Android/mtproto/core/schemes"
        var outputDir: String = "/Users/tamimattafi/Projects/Android/mtproto/core/build/generated/mtproto"
        var basePackage: String = "com.attafitamim.mtproto.sample"
        MTGenerator(schemeFilesDir, outputDir, basePackage).startGeneration()
    }
}