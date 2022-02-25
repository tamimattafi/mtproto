package com.attafitamim.mtproto.core.generator.plugin

import com.attafitamim.mtproto.core.generator.poet.generation.MTGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class MTGenerationTask : DefaultTask() {

    @Input
    var outputDir: String = "${project.buildDir}/generated/mtproto"

    @Input
    var schemeFilesDir: String = "/schemes"

    @Input
    var basePackage: String = "com.attafitamim.mtproto.core.generator"

    @TaskAction
    fun generate() {
        MTGenerator(schemeFilesDir, basePackage, basePackage).startGeneration()
    }
}