package com.attafitamim.mtproto.core.generator.poet.generation

import com.attafitamim.mtproto.core.generator.scheme.parsers.TLObjectParser
import com.attafitamim.mtproto.core.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.core.generator.poet.factories.FileSpecFactory
import com.attafitamim.mtproto.core.generator.poet.factories.PropertySpecFactory
import com.attafitamim.mtproto.core.generator.poet.factories.TypeNameFactory
import com.attafitamim.mtproto.core.generator.poet.factories.TypeSpecFactory
import com.attafitamim.mtproto.core.generator.scheme.parsers.TLContainerParser
import com.attafitamim.mtproto.core.generator.scheme.parsers.TLMethodParser
import com.attafitamim.mtproto.core.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLMethodSpec
import com.attafitamim.mtproto.core.generator.syntax.CONSTANT_NAME_SEPARATOR
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
        val methodSpecs = ArrayList<TLMethodSpec>()
        val objectSpecs = ArrayList<TLObjectSpec>()
        val containerSpecs = ArrayList<TLContainerSpec>()

        schemeFile.forEachLine { schemeLine ->
            when {
                schemeLine == "---types---" -> generatingTypes = true
                schemeLine.contains("---functions---", true) -> generatingTypes = false
                else -> {
                    when {
                        generatingTypes && TLObjectParser.isValidObjectScheme(schemeLine) -> {
                            val tlObjectSpecs = TLObjectParser.parseObject(
                                schemeLine,
                                containerSpecs
                            )

                            objectSpecs.add(tlObjectSpecs)
                        }

                        TLMethodParser.isValidMethodScheme(schemeLine) -> {
                            val tlMethodSpec = TLMethodParser.parseMethod(
                                schemeLine,
                                containerSpecs
                            )

                            methodSpecs.add(tlMethodSpec)
                        }

                        TLContainerParser.isValidContainerScheme(schemeLine) -> {
                            val tlContainer = TLContainerParser.parseContainer(
                                schemeLine,
                                containerSpecs
                            )

                            containerSpecs.add(tlContainer)
                        }
                    }
                }
            }
        }

        println("TL: Successfully parse ${methodSpecs.size} methods and ${objectSpecs.size} types")

        val typeNameFactory = TypeNameFactory(outputPackage)
        val propertySpecFactory = PropertySpecFactory(typeNameFactory)
        val typeSpecFactory = TypeSpecFactory(typeNameFactory, propertySpecFactory)
        val fileSpecFactory = FileSpecFactory(typeNameFactory, typeSpecFactory)

        containerSpecs.forEach { tlContainerSpec ->
            val fileSpec = fileSpecFactory.createContainerFileSpec(tlContainerSpec)
            fileSpec.writeTo(sourceCodePath)
        }

        val objectsGroup = objectSpecs.groupBy { mtObjectSpec ->
            mtObjectSpec.superType
        }

        objectsGroup.forEach { (baseObject, objectVariants) ->
            val filteredObjectVariants = objectVariants.groupBy(TLObjectSpec::name)
                .flatMap { (_, group) ->
                    if (group.size == 1) group
                    else group.map { tlObjectSpec ->
                        val newName = buildString {
                            append(
                                tlObjectSpec.name,
                                CONSTANT_NAME_SEPARATOR,
                                tlObjectSpec.constructorHash
                            )
                        }

                        tlObjectSpec.copy(name = newName)
                    }
                }

            val fileSpec = fileSpecFactory.createObjectFileSpec(baseObject, filteredObjectVariants)
            fileSpec.writeTo(sourceCodePath)
        }

        methodSpecs.forEach { tlMethodSpec ->
            val fileSpec = fileSpecFactory.createMethodFileSpecs(tlMethodSpec)
            fileSpec.writeTo(sourceCodePath)
        }
    }
}