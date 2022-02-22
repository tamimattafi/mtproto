package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.exceptions.MTObjectParseException
import com.attafitamim.mtproto.core.generator.specs.MTObjectSpec
import com.attafitamim.mtproto.core.generator.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.types.TLObjectSpecs
import com.attafitamim.mtproto.core.generator.types.TLPropertySpecs
import com.attafitamim.mtproto.core.generator.utils.addPrimaryConstructor
import com.attafitamim.mtproto.core.generator.utils.createConstantPropertySpec
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase
import com.attafitamim.mtproto.core.objects.MTMethod
import com.attafitamim.mtproto.core.objects.MTObject
import com.attafitamim.mtproto.core.stream.MTInputStream
import com.squareup.kotlinpoet.*
import org.gradle.internal.impldep.org.apache.commons.lang.text.StrBuilder
import kotlin.math.pow

class TypeSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val propertySpecFactory: PropertySpecFactory
) {

    private companion object {
        const val IDENTICAL_OBJECTS_SEPARATOR = "_"
    }

    fun createObjectSpec(
        mtSuperObjectSpec: MTTypeSpec.Object,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec {
        val superClassName = typeNameFactory.createClassName(mtSuperObjectSpec)
        val classTypeBuilder = TypeSpec.classBuilder(superClassName)
            .addModifiers(KModifier.SEALED)
            .superclass(MTObject::class)

        mtVariantObjectSpecs
            .groupBy(MTObjectSpec::name)
            .forEach { group ->
                if (group.value.size == 1) {
                    val objectClass = createObjectSpec(superClassName, group.value.first())
                    classTypeBuilder.addType(objectClass)
                } else {
                    group.value.forEach { tlObjectSpecs ->
                        tlObjectSpecs.name = StrBuilder()
                            .append(tlObjectSpecs.name)
                            .append(IDENTICAL_OBJECTS_SEPARATOR)
                            .append(Integer.toHexString(tlObjectSpecs.hash))
                            .toString()

                        val objectClass = createObjectSpec(superClassName, tlObjectSpecs)
                        classTypeBuilder.addType(objectClass)
                    }
                }
            }

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addBaseObjectParseFunction(superClassName, mtVariantObjectSpecs)
            .build()

        return classTypeBuilder
            .addType(companionObjectBuilder)
            .build()
    }

    fun createObjectSpec(
        superClassName: ClassName,
        mtVariantObjectSpec: MTObjectSpec
    ): TypeSpec {
        val className = typeNameFactory.createClassName(mtVariantObjectSpec.name, superClassName)
        val classBuilder = TypeSpec.classBuilder(className)
            .superclass(superClassName)

        val objectProperties = mtVariantObjectSpec.propertiesSpecs?.map { mtPropertySpec ->
            propertySpecFactory.createPropertySpec(mtPropertySpec)
        }

        if (!objectProperties.isNullOrEmpty()) classBuilder
            .addPrimaryConstructor(objectProperties)
            .addModifiers(KModifier.DATA)

        val hashConstant = createConstantPropertySpec(
            mtVariantObjectSpec::hash.name,
            mtVariantObjectSpec.hash
        )

        val hashPropertySpec = PropertySpec.builder(MTObjectSpec::hash.name, Int::class)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%L", hashConstant.name)
            .build()

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addProperty(hashConstant)
            .addObjectParseFunction(mtVariantObjectSpec, className)
            .build()

        return classBuilder.addType(companionObjectBuilder)
            .addProperty(hashPropertySpec)
            .addObjectSerializeFunction(mtVariantObjectSpec)
            .addKdoc("Scheme: ${mtVariantObjectSpec.rawScheme}")
            .build()
    }

    fun createMethodSpec(mtObjectSpec: MTObjectSpec): TypeSpec {

    }

    fun TypeSpec.Builder.addBaseObjectParseFunction(
        superClassName: ClassName,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec.Builder = this.apply {
        val bufferClassName = MTInputStream::class.asClassName()

        val hashParameterName = TLObjectSpecs::hash.name
        val functionBuilder = FunSpec.builder(MTMethod<*>::parseResponse.name)
            .addParameter(Constants.INPUT_STREAM_PARAMETER_NAME, bufferClassName)
            .addParameter(hashParameterName, Int::class)

        functionBuilder.beginControlFlow("return when($hashParameterName)")

        mtVariantObjectSpecs
            .groupBy(MTObjectSpec::name)
            .forEach { group ->
                if (group.value.size == 1) {

                    val objectClass = typeNameFactory.createClassName(group.value.first().name, superClassName)
                    functionBuilder.addStatement(
                        "${objectClass.simpleName}.${TLObjectSpecs::hash.name.toUpperCase()} -> ${objectClass.simpleName}.${MTMethod<*>::parseResponse.name}(${Constants.INPUT_STREAM_PARAMETER_NAME}, $hashParameterName)"
                    )
                } else {
                    group.value.forEach { mtObjectSpec ->
                        mtObjectSpec.name = StrBuilder()
                            .append(mtObjectSpec.name)
                            .append(IDENTICAL_OBJECTS_SEPARATOR)
                            .append(Integer.toHexString(mtObjectSpec.hash))
                            .toString()

                        val objectClass = typeNameFactory.createClassName(mtObjectSpec.name, superClassName)
                        functionBuilder.addStatement(
                            "${objectClass.simpleName}.${TLObjectSpecs::hash.name.toUpperCase()} -> ${objectClass.simpleName}.${MTMethod<*>::parseResponse.name}(${Constants.INPUT_STREAM_PARAMETER_NAME}, $hashParameterName)"
                        )
                    }
                }
            }

        functionBuilder.addStatement("else -> throw ${MTObjectParseException::class.qualifiedName}(${superClassName.simpleName}::class, $hashParameterName)").endControlFlow()
        val functionSpec = functionBuilder.returns(superClassName).build()
        addFunction(functionSpec)
    }

    fun TypeSpec.Builder.addObjectParseFunction(
        mtObjectSpec: MTObjectSpec,
        returnType: ClassName
    ): TypeSpec.Builder = this.apply {
        val functionBuilder = FunSpec.builder(MTMethod<*>::parseResponse.name)
            .addParameter(Constants.INPUT_STREAM_PARAMETER_NAME, MTInputStream::class)
            .addParameter(TLObjectSpecs::hash.name, Int::class)
            .addHashValidationMethod()

        if (mtObjectSpec.hasFlags) {
            functionBuilder.addFlagReadingStatement()
        }

        mtObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
            functionBuilder.addPropertyParsingStatement(mtPropertySpec)
        }

        functionBuilder.addObjectReturnStatement(mtObjectSpec, returnType).returns(returnType)
        val functionSpec = functionBuilder.build()

        addFunction(functionSpec)
    }

    fun FunSpec.Builder.addHashValidationMethod() : FunSpec.Builder = this.apply {
        val hashParameterName = TLObjectSpecs::hash.name
        val hashValidationStatement = "require($hashParameterName == ${hashParameterName.toUpperCase()})"
        addStatement(hashValidationStatement)
    }

    fun FunSpec.Builder.addFlagReadingStatement(): FunSpec.Builder = this.apply {
        val flagReadingStatement = "val ${Constants.FLAGS_PROPERTY_NAME} = ${Constants.INPUT_STREAM_PARAMETER_NAME}.readInt()"
        addStatement(flagReadingStatement)
    }

    fun FunSpec.Builder.addPropertyParsingStatement(mtPropertySpecs: MTPropertySpec): FunSpec.Builder = this.apply {
        if (mtPropertySpecs.type.startsWith(Constants.TL_VECTOR_TYPE_NAME, true)) addVectorParsingStatement(mtPropertySpecs)
        else addTypeParsingStatement(mtPropertySpecs)
    }

    fun FunSpec.Builder.addVectorParsingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        if (!tlPropertySpecs.type.startsWith(Constants.TL_VECTOR_TYPE_NAME, true)) return@apply
        addStatement("")

        val formattedPropertyName = snakeToCamelCase(tlPropertySpecs.name)
        val vectorGenericType = getVectorGenericType(tlPropertySpecs.type)
        val arrayInitializationStatement = "val $formattedPropertyName = ArrayList<$vectorGenericType>()"
        addStatement(arrayInitializationStatement)

        if (tlPropertySpecs.flag != null) {
            val flagPosition = 2.0.pow(tlPropertySpecs.flag).toInt()
            val flagBooleanStatement = "(${Constants.FLAGS_PROPERTY_NAME} and $flagPosition) != 0"
            val flagCheckingStatement = "if ($flagBooleanStatement)"
            beginControlFlow(flagCheckingStatement)
        }

        val integerReadStatement = "${Constants.INPUT_STREAM_PARAMETER_NAME}.readInt()"
        val vectorHashName = "${formattedPropertyName}${Constants.TL_VECTOR_TYPE_NAME}Hash"
        val vectorHashReadingStatement = "val $vectorHashName = $integerReadStatement"
        addStatement(vectorHashReadingStatement)

        val hashValidationStatement = "require($vectorHashName == ${Constants.TL_VECTOR_TYPE_CONSTRUCTOR})"
        addStatement(hashValidationStatement)

        val vectorSizeName = "${formattedPropertyName}${Constants.TL_VECTOR_TYPE_NAME}Size"
        val vectorSizeReadingStatement = "val $vectorSizeName = $integerReadStatement"
        addStatement(vectorSizeReadingStatement)

        val vectorLoopStatement = "for (index in 0 until $vectorSizeName)"
        beginControlFlow(vectorLoopStatement)

        val vectorGenericName = getVectorGenericName(tlPropertySpecs.type)
        val itemPrimitiveType = localTypes[vectorGenericName]
        val itemTypeName = itemPrimitiveType?.simpleName ?: getVectorGenericType(tlPropertySpecs.type).simpleName
        val itemHashName = "${formattedPropertyName}ItemHash"
        val itemHashInitializationStatement = "val $itemHashName = $integerReadStatement"

        val itemReadStatement = if (itemPrimitiveType != null) {
            "${Constants.INPUT_STREAM_PARAMETER_NAME}.read$itemTypeName()"
        } else {
            addStatement(itemHashInitializationStatement)
            "$itemTypeName.${MTMethod<*>::parseResponse.name}(${Constants.INPUT_STREAM_PARAMETER_NAME}, $itemHashName)"
        }

        val itemName = "${formattedPropertyName}Item"
        val itemInitializationStatement = "val $itemName = $itemReadStatement"
        addStatement(itemInitializationStatement)

        val itemInsertionStatement = "${formattedPropertyName}.add($itemName)"
        addStatement(itemInsertionStatement).endControlFlow()

        if (tlPropertySpecs.flag != null) {
            endControlFlow()
        }
    }

    fun FunSpec.Builder.addTypeParsingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        val formattedPropertyName = snakeToCamelCase(tlPropertySpecs.name)

        val primitiveType = localTypes[tlPropertySpecs.type]
        val objectTypeName = primitiveType?.simpleName ?: createDataObjectClassName(tlPropertySpecs.type).simpleName
        val objectHashName = "${formattedPropertyName}Hash"

        val readStatement = if (primitiveType != null) {
            "${Constants.INPUT_STREAM_PARAMETER_NAME}.read$objectTypeName()"
        } else {
            "$objectTypeName.${MTMethod<*>::parseResponse.name}(${Constants.INPUT_STREAM_PARAMETER_NAME}, $objectHashName)"
        }

        if (tlPropertySpecs.flag != null) {
            val flagPosition = 2.0.pow(tlPropertySpecs.flag).toInt()
            val flagBooleanStatement = "(${Constants.FLAGS_PROPERTY_NAME} and $flagPosition) != 0"

            if (objectTypeName == Boolean::class.simpleName) {
                val propertyDeclarationStatement = "val $formattedPropertyName: $objectTypeName = $flagBooleanStatement"
                addStatement(propertyDeclarationStatement)
            } else {
                addStatement("")
                val propertyDeclarationStatement = "var $formattedPropertyName: $objectTypeName? = null"
                addStatement(propertyDeclarationStatement)

                val flagCheckingStatement = "if ($flagBooleanStatement)"
                beginControlFlow(flagCheckingStatement)

                if (primitiveType == null) {
                    val hashReadingStatement = "val $objectHashName = ${Constants.INPUT_STREAM_PARAMETER_NAME}.readInt()"
                    addStatement(hashReadingStatement)
                }

                val readNullableValueStatement = "$formattedPropertyName = $readStatement"
                addStatement(readNullableValueStatement).endControlFlow()
            }
        } else {
            if (primitiveType == null) {
                val hashReadingStatement = "val $objectHashName = ${Constants.INPUT_STREAM_PARAMETER_NAME}.readInt()"
                addStatement(hashReadingStatement)
            }

            val propertyDeclarationStatement = "val $formattedPropertyName: $objectTypeName = $readStatement"
            addStatement(propertyDeclarationStatement)
        }
    }
}