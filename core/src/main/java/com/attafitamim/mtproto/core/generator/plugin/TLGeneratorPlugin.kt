package com.attafitamim.mtproto.core.generator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class TLGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateProtoClasses", TLGenerationTask::class.java) {
            it.group = "mtproto"
        }
    }

}