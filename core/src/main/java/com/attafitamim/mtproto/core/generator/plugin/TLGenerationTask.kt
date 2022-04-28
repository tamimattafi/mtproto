package com.attafitamim.mtproto.core.generator.plugin

import com.attafitamim.mtproto.core.generator.poet.generation.TLGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class TLGenerationTask : DefaultTask() {

    @Input
    lateinit var outputDir: String

    @Input
    lateinit var schemeFilesDir: String

    @Input
    lateinit var basePackage: String

    @TaskAction
    fun generate() {
        TLGenerator(schemeFilesDir, outputDir, basePackage).startGeneration()
    }
}