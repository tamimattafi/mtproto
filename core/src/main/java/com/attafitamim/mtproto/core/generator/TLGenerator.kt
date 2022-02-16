package com.attafitamim.mtproto.core.generator

import org.gradle.api.Plugin
import org.gradle.api.Project

class TLGenerator : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateProtoClasses", TLGenerationTask::class.java) {
            it.group = "proto"
        }
    }

}