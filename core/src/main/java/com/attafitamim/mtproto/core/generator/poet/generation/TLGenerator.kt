package com.attafitamim.mtproto.core.generator.poet.generation

import com.attafitamim.mtproto.core.generator.scheme.parsers.TLObjectParser
import com.attafitamim.mtproto.core.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.core.generator.poet.factories.FileSpecFactory
import com.attafitamim.mtproto.core.generator.poet.factories.PropertySpecFactory
import com.attafitamim.mtproto.core.generator.poet.factories.TypeNameFactory
import com.attafitamim.mtproto.core.generator.poet.factories.TypeSpecFactory
import org.gradle.api.GradleException
import java.io.File

class TLGenerator(
    private val schemeFilesDir: String,
    private val outputDir: String,
    private val outputPackage: String
) {

    fun startGeneration() {
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
        val methodTlObjects = ArrayList<TLObjectSpec>()
        val typesTlObjects = ArrayList<TLObjectSpec>()

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

        println("TL: Successfully parse ${methodTlObjects.size} methods and ${typesTlObjects.size} types")

        val typeNameFactory = TypeNameFactory(outputPackage)
        val propertySpecFactory = PropertySpecFactory(typeNameFactory)
        val typeSpecFactory = TypeSpecFactory(typeNameFactory, propertySpecFactory)
        val fileSpecFactory = FileSpecFactory(typeNameFactory, typeSpecFactory)

        val objectsGroup = typesTlObjects.groupBy { mtObjectSpec ->
            mtObjectSpec.superType
        }

        objectsGroup.forEach { (baseObject, objectVariants) ->
            val fileSpec = fileSpecFactory.createFileSpec(baseObject, objectVariants)
            fileSpec.writeTo(sourceCodePath)
        }
    }
}