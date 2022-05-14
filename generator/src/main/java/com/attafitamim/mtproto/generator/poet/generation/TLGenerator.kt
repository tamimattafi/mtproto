package com.attafitamim.mtproto.generator.poet.generation

import com.attafitamim.mtproto.generator.poet.factories.TLContainerFactory
import com.attafitamim.mtproto.generator.scheme.parsers.TLObjectParser
import com.attafitamim.mtproto.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.generator.poet.factories.TypeNameFactory
import com.attafitamim.mtproto.generator.poet.factories.TLMethodFactory
import com.attafitamim.mtproto.generator.poet.factories.TLObjectFactory
import com.attafitamim.mtproto.generator.scheme.parsers.TLContainerParser
import com.attafitamim.mtproto.generator.scheme.parsers.TLMethodParser
import com.attafitamim.mtproto.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLMethodSpec
import com.attafitamim.mtproto.generator.syntax.CONSTANT_NAME_SEPARATOR
import com.attafitamim.mtproto.generator.syntax.FUNCTIONS_KEYWORD
import com.attafitamim.mtproto.generator.syntax.TYPES_KEYWORD
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
            filesTree.forEach { file ->
                if (!file.isDirectory) this.parseSchemeFromFile(sourceCodePath, file)
            }
        } else {
            this.parseSchemeFromFile(sourceCodePath, schemesDirectory)
        }
    }

    private fun parseSchemeFromFile(outputPath: File, schemeFile: File) {
        println("TL: Parsing types from ${schemeFile.name}")

        var generatingTypes = true
        val methodSpecs = ArrayList<TLMethodSpec>()
        val objectSpecs = ArrayList<TLObjectSpec>()
        val containerSpecs = ArrayList<TLContainerSpec>()

        schemeFile.forEachLine { line ->
            when {
                line == TYPES_KEYWORD -> generatingTypes = true
                line == FUNCTIONS_KEYWORD -> generatingTypes = false

                generatingTypes && TLObjectParser.isValidObjectScheme(line) -> {
                    val tlObjectSpecs = TLObjectParser.parseObject(line, containerSpecs)
                    objectSpecs.add(tlObjectSpecs)
                }

                TLMethodParser.isValidMethodScheme(line) -> {
                    val tlMethodSpec = TLMethodParser.parseMethod(line, containerSpecs)
                    methodSpecs.add(tlMethodSpec)
                }

                TLContainerParser.isValidContainerScheme(line) -> {
                    val tlContainer = TLContainerParser.parseContainer(line, containerSpecs)
                    containerSpecs.add(tlContainer)
                }
            }
        }

        println(
            """
            TL Parsing Results:
            containers: ${containerSpecs.size}
            objects: ${objectSpecs.size}
            methods: ${methodSpecs.size}
            """.trimIndent()
        )

        generateSourceCode(
            outputPath,
            methodSpecs,
            objectSpecs,
            containerSpecs
        )
    }

    private fun generateSourceCode(
        outputPath: File,
        methodSpecs: ArrayList<TLMethodSpec>,
        objectSpecs: ArrayList<TLObjectSpec>,
        containerSpecs: ArrayList<TLContainerSpec>
    ) {
        val typeNameFactory = TypeNameFactory(outputPackage)

        containerSpecs.forEach { tlContainerSpec ->
            val fileSpec = TLContainerFactory.createFileSpec(
                tlContainerSpec,
                typeNameFactory
            )

            fileSpec.writeTo(outputPath)
        }

        val objectsGroup = objectSpecs.groupBy(TLObjectSpec::superType)
        objectsGroup.forEach { (superType, variants) ->
            val filteredObjectVariants = mutateDuplicates(variants)

            val fileSpec = TLObjectFactory.createFileSpec(
                superType,
                filteredObjectVariants,
                typeNameFactory
            )

            fileSpec.writeTo(outputPath)
        }

        methodSpecs.forEach { tlMethodSpec ->
            val fileSpec = TLMethodFactory.createFileSpec(tlMethodSpec, typeNameFactory)
            fileSpec.writeTo(outputPath)
        }

        println(
            """
            TL Generating Results:
            TLContainer: ${containerSpecs.size}
            TLObject (Base): ${objectsGroup.size}
            TLObject (Variant): ${objectSpecs.size}
            TLMethod: ${methodSpecs.size}
            Package: $outputPackage
            SourceDir: $outputDir
            """.trimIndent()
        )
    }

    private fun mutateDuplicates(variants: List<TLObjectSpec>) =
        variants.groupBy(TLObjectSpec::name).flatMap { (name, duplicates) ->
            if (duplicates.size == 1) {
                duplicates
            } else duplicates.map { tlObjectSpec ->
                val newName = buildString {
                    append(
                        name,
                        CONSTANT_NAME_SEPARATOR,
                        tlObjectSpec.constructorHash
                    )
                }

                tlObjectSpec.copy(name = newName)
            }
        }
}