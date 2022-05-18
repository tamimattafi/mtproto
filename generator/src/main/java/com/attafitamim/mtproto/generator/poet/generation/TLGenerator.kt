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
import com.attafitamim.mtproto.generator.utils.takeIfSingleElement
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
            val filteredObjectVariants = cleanVariants(variants)

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
            TL Generating Finished. Results:
            TLContainers: ${containerSpecs.size}
            TLObjects (Base): ${objectsGroup.size}
            TLObjects (Variant): ${objectSpecs.size}
            TLMethods: ${methodSpecs.size}
            Output Dir: $outputDir
            Output Package: $outputPackage
            """.trimIndent()
        )
    }

    private fun cleanVariants(variants: List<TLObjectSpec>) =
        variants.takeIfSingleElement() ?: variants.groupBy(TLObjectSpec::name)
            .flatMap { (name, duplicates) ->
                duplicates.takeIfSingleElement() ?: mutateDuplicatesName(name, variants)
            }

    private fun mutateDuplicatesName(
        duplicateName: String,
        duplicates: List<TLObjectSpec>
    ) = duplicates.map { tlObjectSpec ->
        val newName = buildString {
            append(duplicateName, CONSTANT_NAME_SEPARATOR, tlObjectSpec.constructorHash)
        }

        tlObjectSpec.copy(name = newName)
    }
}
