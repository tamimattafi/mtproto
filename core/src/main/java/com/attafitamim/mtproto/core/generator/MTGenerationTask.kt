package com.attafitamim.mtproto.core.generator

import com.attafitamim.mtproto.core.exceptions.MTObjectParseException
import com.attafitamim.mtproto.core.generator.classes.TLClassGenerator
import com.attafitamim.mtproto.core.generator.parsers.TLObjectParser
import com.attafitamim.mtproto.core.generator.types.TLObjectSpecs
import com.attafitamim.mtproto.core.objects.MTMethod
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class MTGenerationTask : DefaultTask() {

    @Input
    var outputDir: String = "${project.buildDir}/generated/mtproto"

    @Input
    var schemeFilesDir: String = "/schemes"

    @Input
    var basePackage: String = "com.attafitamim.mtproto.core.generator"

    @TaskAction
    fun generate() {
        val schemesDirectory = File(schemeFilesDir)
        if (!schemesDirectory.exists()) {
            throw GradleException("Schemes directory doesn't exist: $schemeFilesDir")
        }

        val sourceCodePath = File(outputDir)
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
                val typeObjectSpec = TLClassGenerator(basePackage).generateSuperDataObjectClass(group.key, group.value)
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
                val methodFileSpec = TLClassGenerator(basePackage).generateMethodClass(tlMethodSpecs)
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