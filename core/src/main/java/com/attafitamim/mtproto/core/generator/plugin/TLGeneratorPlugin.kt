package com.attafitamim.mtproto.core.generator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class TLGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register(NAME, TLGenerationTask::class.java) { task ->
            task.group = GROUP
        }
    }

    companion object {
        const val NAME = "generateProtoClasses"
        const val GROUP = "mtproto"
    }
}
