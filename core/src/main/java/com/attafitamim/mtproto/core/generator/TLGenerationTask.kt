package com.attafitamim.mtproto.core.generator

import com.attafitamim.mtproto.core.generator.classes.TLClassGenerator
import com.attafitamim.mtproto.core.generator.parsers.TLObjectParser
import com.attafitamim.mtproto.core.generator.types.TLObjectSpecs
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class TLGenerationTask : DefaultTask() {

    @Input
    var outputDir: String = "generated/source/scheme"

    @Input
    var schemeFilesDir: String = ""

    @TaskAction
    fun generate() {
        val schemesDirectory = File(schemeFilesDir)
        if (!schemesDirectory.exists()) {
            throw GradleException("Schemes directory doesn't exist: $schemeFilesDir")
        }

        val sourceCodePath = File(project.buildDir, outputDir)
        if (sourceCodePath.exists()) sourceCodePath.deleteRecursively()
        sourceCodePath.mkdirs()

        if (schemesDirectory.isDirectory) {
            val filesTree = schemesDirectory.walkTopDown()
            filesTree.forEach { file -> if (!file.isDirectory) this.generateSchemeFromFile(sourceCodePath, file) }
        } else this.generateSchemeFromFile(sourceCodePath, schemesDirectory)
    }

    private fun generateSchemeFromFile(sourceCodePath: File, schemeFile: File) {
        println("TL: Parsing types from ${schemeFile.name}")

        var generatingTypes = true
        val methodTlObjects = ArrayList<TLObjectSpecs>()
        val typesTlObjects = ArrayList<TLObjectSpecs>()

        schemeFile.forEachLine { schemeLine ->
            when {
                schemeLine == "---types---" -> generatingTypes = true
                schemeLine.contains("---functions---", true) -> generatingTypes = false
                else -> {
                    val isValidTlObject = TLObjectParser.isValidObjectScheme(schemeLine)
                    if (isValidTlObject) {
                        val tlObject = TLObjectParser.parseObject(schemeLine)
                        if (generatingTypes) typesTlObjects.add(tlObject)
                        else methodTlObjects.add(tlObject)
                    }
                }
            }
        }

        this.generateTypes(sourceCodePath, typesTlObjects)
        this.generateMethods(sourceCodePath, methodTlObjects)

        println("TL: Successfully generated ${methodTlObjects.size} methods and ${typesTlObjects.size} types")
        println("TL: Generated code can be found at ${sourceCodePath.path}")
    }

    private fun generateTypes(sourceCodePath: File, typesTlObjects: ArrayList<TLObjectSpecs>) {
        val tlTypesSpecs = typesTlObjects.groupBy(TLObjectSpecs::superClassName)
        tlTypesSpecs.forEach { group ->
            try {
                val typeObjectSpec = TLClassGenerator.generateSuperDataObjectClass(group.key, group.value)
                typeObjectSpec.writeTo(sourceCodePath)
            } catch (exception: Exception) {
                throw GradleException(
                        """
                        Failed to write Base TL Object generated file
                        Objects group: $group
                        Sub Objects List: ${group.value}
                        Error: ${exception.message}
                    """.trimIndent()
                )
            }
        }
    }

    private fun generateMethods(sourceCodePath: File, typesTlObjects: ArrayList<TLObjectSpecs>) {
        typesTlObjects.forEach { tlMethodSpecs ->
            try {
                val methodFileSpec = TLClassGenerator.generateMethodClass(tlMethodSpecs)
                methodFileSpec.writeTo(sourceCodePath)
            } catch (exception: Exception) {
                throw GradleException(
                        """
                        Failed to write TL Method generated file
                        TLObjectSpecs: $tlMethodSpecs
                        Error: ${exception.message}
                    """.trimIndent()
                )
            }
        }
    }

}