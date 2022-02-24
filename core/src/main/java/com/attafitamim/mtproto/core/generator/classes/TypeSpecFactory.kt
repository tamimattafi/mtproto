package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.exceptions.MTObjectParseException
import com.attafitamim.mtproto.core.generator.constants.*
import com.attafitamim.mtproto.core.generator.specs.MTObjectSpec
import com.attafitamim.mtproto.core.generator.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.utils.*
import com.attafitamim.mtproto.core.objects.MTMethod
import com.attafitamim.mtproto.core.objects.MTObject
import com.attafitamim.mtproto.core.stream.MTInputStream
import com.attafitamim.mtproto.core.stream.MTOutputStream
import com.squareup.kotlinpoet.*
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.math.pow
import kotlin.reflect.KClass

class TypeSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val propertySpecFactory: PropertySpecFactory
) {

    fun createObjectSpec(
        mtSuperObjectSpec: MTTypeSpec.Object,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec {
        val superClassName = typeNameFactory.createClassName(mtSuperObjectSpec)
        val classTypeBuilder = TypeSpec.classBuilder(superClassName)
            .addModifiers(KModifier.SEALED)
            .superclass(MTObject::class)

        mtVariantObjectSpecs.forEach { mtObjectSpec ->
            val objectClass = createObjectSpec(superClassName, mtObjectSpec)
            classTypeBuilder.addType(objectClass)
        }

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addBaseObjectParseFunction(superClassName, mtVariantObjectSpecs)
            .build()

        return classTypeBuilder
            .addType(companionObjectBuilder)
            .build()
    }

    private fun createObjectSpec(
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

        val objectSerializeFunction = createObjectSerializeFunctionSpec(mtVariantObjectSpec)
        return classBuilder.addType(companionObjectBuilder)
            .addProperty(hashPropertySpec)
            .addFunction(objectSerializeFunction)
            .addKdoc("Scheme: ${mtVariantObjectSpec.rawScheme}")
            .build()
    }

    private fun TypeSpec.Builder.addBaseObjectParseFunction(
        superClassName: ClassName,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec.Builder = this.apply {
        val inputStreamClass = MTInputStream::class
        val hashParameterName = MTObject::hash.name
        val hashConstantName = hashParameterName.uppercase()

        val functionBuilder = FunSpec.builder(MTMethod<*>::parseResponse.name)
            .addParameter(inputStreamClass.asParamterName, inputStreamClass)
            .addParameter(hashParameterName, Int::class)
            .addHashValidationMethod()
            .returns(superClassName)

        val whenStatement = StringBuilder()
            .append(RETURN_KEYWORD)
            .append(KEYWORD_SEPARATOR)
            .append(WHEN_KEYWORD)
            .append(PARAMETER_OPEN_PARENTHESIS)
            .append(hashParameterName)
            .append(PARAMETER_CLOSE_PARENTHESIS)
            .toString()

        functionBuilder.beginControlFlow(whenStatement)
        mtVariantObjectSpecs.forEach { mtObjectSpec ->
            val objectClass = typeNameFactory.createClassName(mtObjectSpec.name, superClassName)
            val checkStatement = StringBuilder().append(
                objectClass.simpleName,
                INSTANCE_ACCESS_KEY,
                hashConstantName,
                WHEN_RESULT_ARROW,
                objectClass.simpleName,
                INSTANCE_ACCESS_KEY,
                MTMethod<*>::parseResponse.name,
                PARAMETER_OPEN_PARENTHESIS,
                inputStreamClass,
                PARAMETER_SEPARATOR,
                hashParameterName,
                PARAMETER_CLOSE_PARENTHESIS
            ).toString()

            functionBuilder.addStatement(checkStatement)
        }

        val throwStatement = StringBuilder().append(
            ELSE_KEYWORD,
            WHEN_RESULT_ARROW,
            THROW_KEYWORD,
            KEYWORD_SEPARATOR,
            MTObjectParseException::class.qualifiedName,
            PARAMETER_OPEN_PARENTHESIS,
            superClassName.simpleName,
            CLASS_ACCESS_KEY,
            CLASS_KEYWORD,
            PARAMETER_SEPARATOR,
            hashParameterName,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        functionBuilder.addStatement(throwStatement)
        val functionSpec = functionBuilder.endControlFlow().build()
        addFunction(functionSpec)
    }

    private fun TypeSpec.Builder.addObjectParseFunction(
        mtObjectSpec: MTObjectSpec,
        returnType: ClassName
    ): TypeSpec.Builder = this.apply {
        val inputStreamClass = MTInputStream::class
        val functionBuilder = FunSpec.builder(MTMethod<*>::parseResponse.name)
            .addParameter(inputStreamClass.asParamterName, inputStreamClass)
            .addParameter(MTObject::hash.name, Int::class)
            .addHashValidationMethod()
            .returns(returnType)

        if (mtObjectSpec.hasFlags) {
            functionBuilder.addFlagReadingStatement()
        }

        mtObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
            functionBuilder.addTypeParsingStatement(mtPropertySpec)
        }

        functionBuilder.addObjectReturnStatement(mtObjectSpec, returnType)
        val functionSpec = functionBuilder.build()

        addFunction(functionSpec)
    }

    fun createObjectSerializeFunctionSpec(
        mtVariantObjectSpec: MTObjectSpec
    ): FunSpec {
        val bufferClassName = MTOutputStream::class.asClassName()
        val bufferParameterName = titleToCamelCase(bufferClassName.simpleName)

        val functionBuilder = FunSpec.builder(MTObject::serialize.name)
            .addParameter(bufferParameterName, bufferClassName)
            .addModifiers(KModifier.OVERRIDE)

        val hashSerializationStatement = createPropertySerializeStatement(
            MTObject::hash.name,
            Int::class
        )

        functionBuilder.addStatement(hashSerializationStatement)
        if (mtVariantObjectSpec.hasFlags) {
            functionBuilder.addStatement("")

            val flagsInitializationStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                FLAGS_PROPERTY_NAME,
                INITIALIZATION_SIGN,
                FLAGS_DEFAULT_VALUE
            ).toString()

            functionBuilder.addStatement(flagsInitializationStatement)
            mtVariantObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
                if (mtPropertySpec.flag != null) {
                    val flaggingStatement = createFlaggingStatement(
                        mtPropertySpec,
                        mtPropertySpec.flag
                    )

                    functionBuilder.addStatement(flaggingStatement)
                }
            }

            val flagsSerializationStatement = createPropertySerializeStatement(
                FLAGS_PROPERTY_NAME,
                Int::class
            )

            functionBuilder.addStatement(flagsSerializationStatement)
        }

        mtVariantObjectSpec.propertiesSpecs?.forEach { tlPropertySpecs ->
            functionBuilder.createPropertySerializeStatement(tlPropertySpecs)
        }

        return functionBuilder.build()
    }

    fun createPropertySerializeStatement(mtPropertySpec: MTPropertySpec) {

    }

    private fun createFlaggingStatement(mtPropertySpec: MTPropertySpec, flag: Int): String {
        val isBooleanProperty = mtPropertySpec.typeSpec is MTTypeSpec.Local &&
                mtPropertySpec.typeSpec.clazz == Boolean::class

        val flagCheckStatement = if (isBooleanProperty) mtPropertySpec.name
        else StringBuilder().append(
            mtPropertySpec.name,
            NOT_EQUAL_SIGN,
            NULL_KEYWORD
        ).toString()

        val flagPosition = 2.0.pow(flag).toInt()
        return StringBuilder().append(
            FLAGS_PROPERTY_NAME,
            INITIALIZATION_SIGN,
            IF_KEYWORD,
            KEYWORD_SEPARATOR,
            PARAMETER_OPEN_PARENTHESIS,
            flagCheckStatement,
            PARAMETER_CLOSE_PARENTHESIS,
            KEYWORD_SEPARATOR,
            PARAMETER_OPEN_PARENTHESIS,
            FLAGS_PROPERTY_NAME,
            OR_KEYWORD,
            flagPosition,
            PARAMETER_CLOSE_PARENTHESIS,
            KEYWORD_SEPARATOR,
            ELSE_KEYWORD,
            KEYWORD_SEPARATOR,
            PARAMETER_OPEN_PARENTHESIS,
            AND_KEYWORD,
            flagPosition,
            INSTANCE_ACCESS_KEY,
            INVERT_METHOD,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()
    }

    fun FunSpec.Builder.addHashValidationMethod() : FunSpec.Builder = this.apply {

    }

    fun FunSpec.Builder.addFlagReadingStatement(): FunSpec.Builder = this.apply {

    }

    fun FunSpec.Builder.addTypeParsingStatement(mtPropertySpec: MTPropertySpec) = this.apply {

    }

    fun FunSpec.Builder.addObjectReturnStatement(
        tlObjectSpecs: MTObjectSpec,
        returnType: ClassName
    ) : FunSpec.Builder = this.apply {
        addStatement("")
        addStatement("return ${returnType.simpleName}(")

        tlObjectSpecs.propertiesSpecs?.forEach { tlPropertySpecs ->
            val formattedPropertyName = snakeToCamelCase(tlPropertySpecs.name)
            addStatement("$formattedPropertyName,")
        }

        addStatement(")")
    }

    private fun createPropertySerializeStatement(
        name: String,
        typeSpec: MTTypeSpec
    ): String = when(typeSpec) {
        is MTTypeSpec.Object -> createObjectSerializeStatement(name)
        is MTTypeSpec.Generic.Parameter -> createPropertySerializeStatement(name, typeSpec.type)
        is MTTypeSpec.Generic.Variable -> createPropertySerializeStatement(name, typeSpec.superType)
        is MTTypeSpec.Local -> createPropertySerializeStatement(name, typeSpec.clazz)
        is MTTypeSpec.Structure.Collection -> createCollectionSerializeStatement(name, typeSpec.elementGeneric)
    }

    private fun createObjectSerializeStatement(name: String): String
        = StringBuilder().append(
            name,
            INSTANCE_ACCESS_KEY,
            MTObject::serialize.name,
            PARAMETER_OPEN_PARENTHESIS,
            OUTPUT_STREAM_NAME,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

    private fun createCollectionSerializeStatement(
        name: String,
        elementGeneric: MTTypeSpec.Generic
    ): String {

    }

    private fun createPropertySerializeStatement(
        name: String,
        type: KClass<*>
    ): String {
        val serializeMethod = when(type) {
            Boolean::class -> MTOutputStream::writeBoolean
            Byte::class -> MTOutputStream::writeByte
            Int::class -> MTOutputStream::writeInt
            Long::class -> MTOutputStream::writeLong
            Double::class -> MTOutputStream::writeDouble
            String::class -> MTOutputStream::writeString
            ByteArray::class -> MTOutputStream::writeByteArray
            MTInputStream::class -> MTOutputStream::writeInputBuffer
            else -> throw Exception()
        }

        return StringBuilder().append(
            INPUT_STREAM_NAME,
            INSTANCE_ACCESS_KEY,
            serializeMethod.name,
            PARAMETER_OPEN_PARENTHESIS,
            name,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()
    }
}