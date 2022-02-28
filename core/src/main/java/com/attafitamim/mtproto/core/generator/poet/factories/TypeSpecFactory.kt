package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.exceptions.MTObjectParseException
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.scheme.specs.MTObjectSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.utils.*
import com.attafitamim.mtproto.core.serialization.helpers.getTypeParseMethod
import com.attafitamim.mtproto.core.serialization.helpers.getTypeSerializeMethod
import com.attafitamim.mtproto.core.types.MTMethod
import com.attafitamim.mtproto.core.types.MTObject
import com.attafitamim.mtproto.core.serialization.streams.MTInputStream
import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream
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
        superClassName: ClassName,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec {
        val classTypeBuilder = TypeSpec.interfaceBuilder(superClassName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(MTObject::class)

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
            .addSuperinterface(superClassName)

        if (!mtVariantObjectSpec.genericVariables.isNullOrEmpty()) {
            val typeVariables = mtVariantObjectSpec.genericVariables
                .values
                .map(typeNameFactory::createTypeVariableName)

            classBuilder.addTypeVariables(typeVariables)
        }

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
            .addKdoc(mtVariantObjectSpec.rawScheme)
            .build()
    }

    private fun TypeSpec.Builder.addBaseObjectParseFunction(
        superClassName: ClassName,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec.Builder = this.apply {
        val hashParameterName = MTObject::hash.name
        val hashConstantName = hashParameterName.uppercase()

        val functionBuilder = FunSpec.builder(MTMethod<*>::parse.name)
            .addParameter(INPUT_STREAM_NAME, MTInputStream::class)
            .addParameter(hashParameterName, Int::class)
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
                MTMethod<*>::parse.name,
                PARAMETER_OPEN_PARENTHESIS,
                INPUT_STREAM_NAME,
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
        val hashParameterName = MTObject::hash.name
        val hashConstantName = hashParameterName.uppercase()

        val hashValidationStatement = StringBuilder().append(
            REQUIRE_METHOD,
            PARAMETER_OPEN_PARENTHESIS,
            hashParameterName,
            EQUAL_SIGN,
            hashConstantName,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        val functionBuilder = FunSpec.builder(MTMethod<*>::parse.name)
            .addParameter(INPUT_STREAM_NAME, inputStreamClass)
            .addParameter(MTObject::hash.name, Int::class)
            .addStatement(hashValidationStatement)
            .returns(returnType)

        if (mtObjectSpec.hasFlags) {
            functionBuilder.addFlagReadingStatement()
        }

        mtObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
            functionBuilder.addPropertyParseStatement(
                mtPropertySpec.name,
                mtPropertySpec.typeSpec,
                mtPropertySpec.flag
            )
        }

        functionBuilder.addObjectReturnStatement(mtObjectSpec, returnType)
        val functionSpec = functionBuilder.build()

        addFunction(functionSpec)
    }

    private fun createObjectSerializeFunctionSpec(
        mtVariantObjectSpec: MTObjectSpec
    ): FunSpec {
        val functionBuilder = FunSpec.builder(MTObject::serialize.name)
            .addParameter(OUTPUT_STREAM_NAME, MTOutputStream::class)
            .addModifiers(KModifier.OVERRIDE)
            .addLocalPropertySerializeStatement(MTObject::hash.name, Int::class)

        if (mtVariantObjectSpec.hasFlags) {
            val flagsInitializationStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                FLAGS_PROPERTY_NAME,
                INITIALIZATION_SIGN,
                FLAGS_DEFAULT_VALUE
            ).toString()

            functionBuilder.addStatement(flagsInitializationStatement)
            mtVariantObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
                if (mtPropertySpec.flag != null) functionBuilder.addFlaggingStatement(
                    mtPropertySpec,
                    mtPropertySpec.flag
                )
            }

            functionBuilder.addLocalPropertySerializeStatement(FLAGS_PROPERTY_NAME, Int::class)
        }

        mtVariantObjectSpec.propertiesSpecs?.forEach { tlPropertySpecs ->
            functionBuilder.addPropertySerializeStatement(
                tlPropertySpecs.name,
                tlPropertySpecs.typeSpec,
                tlPropertySpecs.flag
            )
        }

        return functionBuilder.build()
    }

    private fun FunSpec.Builder.addFlaggingStatement(
        mtPropertySpec: MTPropertySpec,
        flag: Int
    ): FunSpec.Builder = this.apply {
        val flagCheckStatement = if (mtPropertySpec.typeSpec == MTTypeSpec.Flag) {
            mtPropertySpec.name
        } else {
            StringBuilder().append(
                mtPropertySpec.name,
                NOT_EQUAL_SIGN,
                NULL_KEYWORD
            ).toString()
        }

        val flagPosition = 2.0.pow(flag).toInt()
        val flaggingStatement = StringBuilder().append(
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
            FLAGS_PROPERTY_NAME,
            AND_KEYWORD,
            flagPosition,
            INSTANCE_ACCESS_KEY,
            INVERT_METHOD,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        addStatement(flaggingStatement)
    }

    fun FunSpec.Builder.addFlagReadingStatement(): FunSpec.Builder = this.apply {

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

    private fun FunSpec.Builder.addPropertySerializeStatement(
        name: String,
        typeSpec: MTTypeSpec,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        when(typeSpec) {
            is MTTypeSpec.Object -> addObjectSerializeStatement(name, flag)
            is MTTypeSpec.Generic.Parameter -> addPropertySerializeStatement(name, typeSpec.type, flag)
            is MTTypeSpec.Generic.Variable -> addPropertySerializeStatement(name, typeSpec.superType, flag)
            is MTTypeSpec.Primitive -> addLocalPropertySerializeStatement(name, typeSpec.clazz, flag)
            is MTTypeSpec.Structure.Collection -> addCollectionSerializeStatement(name, typeSpec.elementGeneric, flag)
            is MTTypeSpec.Flag -> { /* Do not serialize to stream */}
            is MTTypeSpec.Type -> { /* TODO: use generic serializer */ }
        }
    }

    private fun FunSpec.Builder.addPropertyParseStatement(
        name: String,
        typeSpec: MTTypeSpec,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        when(typeSpec) {
            is MTTypeSpec.Object -> addObjectParseStatement(name, typeSpec, flag)
            is MTTypeSpec.Generic.Parameter -> addPropertyParseStatement(name, typeSpec.type, flag)
            is MTTypeSpec.Generic.Variable -> addPropertyParseStatement(name, typeSpec.superType, flag)
            is MTTypeSpec.Primitive -> addLocalPropertyParseStatement(name, typeSpec.clazz, flag)
            is MTTypeSpec.Structure.Collection -> addCollectionParseStatement(name, typeSpec.elementGeneric, flag)
            is MTTypeSpec.Flag -> { /* Do not serialize to stream */}
            is MTTypeSpec.Type -> { /* TODO: use generic serializer */ }
        }
    }

    private fun FunSpec.Builder.addObjectSerializeStatement(
        name: String,
        flag: Int?
    ): FunSpec.Builder = this.apply {
        val objectSerializeStatement = StringBuilder().append(
            name,
            INSTANCE_ACCESS_KEY,
            MTObject::serialize.name,
            PARAMETER_OPEN_PARENTHESIS,
            OUTPUT_STREAM_NAME,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        addPropertySerializeStatement(name, objectSerializeStatement, flag)
    }

    private fun FunSpec.Builder.addObjectParseStatement(
        name: String,
        typeSpec: MTTypeSpec.Object,
        flag: Int?
    ): FunSpec.Builder = this.apply {
        val objectClassName = typeNameFactory.createClassName(typeSpec)
        addParseFlagCheckStatement(name, objectClassName, flag) {
            val objectHashName = StringBuilder().append(
                name,
                camelToTitleCase(MTObject::hash.name)
            ).toString()

            addLocalPropertyParseStatement(objectHashName, Int::class)
            val objectParseStatement = StringBuilder().append(
                objectClassName.simpleName,
                INSTANCE_ACCESS_KEY,
                MTMethod<*>::parse.name,
                PARAMETER_OPEN_PARENTHESIS,
                OUTPUT_STREAM_NAME,
                PARAMETER_SEPARATOR,
                objectHashName,
                PARAMETER_CLOSE_PARENTHESIS
            ).toString()

            addStatement(objectParseStatement)
        }
    }

    private fun FunSpec.Builder.addCollectionSerializeStatement(
        name: String,
        elementGeneric: MTTypeSpec.Generic,
        flag: Int?
    ): FunSpec.Builder = addSerializeFlagCheckStatement(name, flag) {
        val collectionSizeName = StringBuilder().append(
            name,
            INSTANCE_ACCESS_KEY,
            List<*>::size.name
        ).toString()

        addLocalPropertySerializeStatement(collectionSizeName, Int::class)

        val iterationStatement = StringBuilder().append(
            name,
            INSTANCE_ACCESS_KEY,
            FOR_EACH_METHOD
        ).toString()

        beginControlFlow(iterationStatement)
        addPropertySerializeStatement(IT_KEYWORD, elementGeneric)
        endControlFlow()
    }

    private fun FunSpec.Builder.addCollectionParseStatement(
        name: String,
        elementGeneric: MTTypeSpec.Generic,
        flag: Int?
    ): FunSpec.Builder = this.apply {

    }

    private fun FunSpec.Builder.addLocalPropertySerializeStatement(
        name: String,
        type: KClass<*>,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        val serializeMethod = getTypeSerializeMethod(type)

        val serializeStatement = StringBuilder().append(
            OUTPUT_STREAM_NAME,
            INSTANCE_ACCESS_KEY,
            serializeMethod.name,
            PARAMETER_OPEN_PARENTHESIS,
            name,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        addPropertySerializeStatement(name, serializeStatement, flag)
    }

    private fun FunSpec.Builder.addPropertySerializeStatement(
        name: String,
        serializeStatement: String,
        flag: Int? = null
    ): FunSpec.Builder = addSerializeFlagCheckStatement(name, flag) {
        addStatement(serializeStatement)
    }

    private fun FunSpec.Builder.addSerializeFlagCheckStatement(
        name: String,
        flag: Int?,
        action: FunSpec.Builder.() -> FunSpec.Builder
    ): FunSpec.Builder = this.apply {
        if (flag != null) {
            val flagCheckStatement = StringBuilder().append(
                IF_KEYWORD,
                PARAMETER_OPEN_PARENTHESIS,
                name,
                NOT_EQUAL_SIGN,
                NULL_KEYWORD,
                PARAMETER_CLOSE_PARENTHESIS,
            ).toString()

            beginControlFlow(flagCheckStatement)
            action()
            endControlFlow()
        } else {
            action()
        }
    }

    private fun FunSpec.Builder.addParseFlagCheckStatement(
        name: String,
        className: ClassName,
        flag: Int?,
        action: FunSpec.Builder.() -> FunSpec.Builder
    ): FunSpec.Builder = this.apply {
        if (flag != null) {
            val definitionStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                TYPE_SIGN,
                KEYWORD_SEPARATOR,
                className.simpleName,
                NULLABLE_SIGN,
                INITIALIZATION_SIGN,
                NULL_KEYWORD
            ).toString()

            addStatement(definitionStatement)
            val flagPosition = 2.0.pow(flag).toInt()
            val flagCheckStatement = StringBuilder().append(
                IF_KEYWORD,
                PARAMETER_OPEN_PARENTHESIS,
                PARAMETER_OPEN_PARENTHESIS,
                FLAGS_PROPERTY_NAME,
                AND_KEYWORD,
                flagPosition,
                PARAMETER_CLOSE_PARENTHESIS,
                NOT_EQUAL_SIGN,
                0,
                PARAMETER_CLOSE_PARENTHESIS
            ).toString()

            beginControlFlow(flagCheckStatement)
            action()
            endControlFlow()
        } else {
            action()
        }
    }

    private fun FunSpec.Builder.addLocalPropertyParseStatement(
        name: String,
        type: KClass<*>,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        val parseMethod = getTypeParseMethod(type)

        val parseStatement = StringBuilder().append(
            INPUT_STREAM_NAME,
            INSTANCE_ACCESS_KEY,
            parseMethod.name,
            PARAMETER_OPEN_PARENTHESIS,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        addPropertyParseStatement(
            name,
            type.asClassName(),
            parseStatement,
            flag
        )
    }

    private fun FunSpec.Builder.addPropertyParseStatement(
        name: String,
        className: ClassName,
        parseStatement: String,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        if (flag != null) {
            val definitionStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                TYPE_SIGN,
                KEYWORD_SEPARATOR,
                className.simpleName,
                NULLABLE_SIGN,
                INITIALIZATION_SIGN,
                NULL_KEYWORD
            ).toString()

            addStatement(definitionStatement)
            val flagPosition = 2.0.pow(flag).toInt()
            val flagCheckStatement = StringBuilder().append(
                IF_KEYWORD,
                PARAMETER_OPEN_PARENTHESIS,
                PARAMETER_OPEN_PARENTHESIS,
                FLAGS_PROPERTY_NAME,
                AND_KEYWORD,
                flagPosition,
                PARAMETER_CLOSE_PARENTHESIS,
                NOT_EQUAL_SIGN,
                0,
                PARAMETER_CLOSE_PARENTHESIS
            ).toString()

            beginControlFlow(flagCheckStatement)
            val assignStatement = StringBuilder().append(
                name,
                INITIALIZATION_SIGN,
                parseStatement
            ).toString()

            addStatement(assignStatement)
            endControlFlow()
        } else {
            val initializationStatement = StringBuilder().append(
                CONSTANT_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                TYPE_SIGN,
                KEYWORD_SEPARATOR,
                className.simpleName,
                INITIALIZATION_SIGN,
                parseStatement
            ).toString()

            addStatement(initializationStatement)
        }
    }
}