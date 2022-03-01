package com.attafitamim.mtproto.core.generator.plugin

import com.attafitamim.mtproto.core.generator.poet.generation.MTGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class MTGenerationTask : DefaultTask() {

    @Input
    lateinit var outputDir: String

    @Input
    lateinit var schemeFilesDir: String

    @Input
    lateinit var basePackage: String

    @TaskAction
    fun generate() {
        MTGenerator(schemeFilesDir, outputDir, basePackage).startGeneration()
    }
}