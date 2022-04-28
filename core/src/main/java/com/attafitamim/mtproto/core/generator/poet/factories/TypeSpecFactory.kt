package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.exceptions.MTObjectParseException
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.scheme.specs.MTObjectSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.utils.*
import com.attafitamim.mtproto.core.serialization.helpers.SerializationHelper
import com.attafitamim.mtproto.core.serialization.helpers.getTypeParseMethod
import com.attafitamim.mtproto.core.serialization.helpers.getTypeSerializeMethod
import com.attafitamim.mtproto.core.types.MTMethod
import com.attafitamim.mtproto.core.types.MTObject
import com.attafitamim.mtproto.core.serialization.streams.MTOutputStream
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.StringBuilder
import kotlin.math.pow
import kotlin.reflect.KClass

class TypeSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val propertySpecFactory: PropertySpecFactory
) {

    fun createObjectSpec(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec {
        val classTypeBuilder = TypeSpec.interfaceBuilder(superClassName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(MTObject::class)

        if (superTypeVariables != null) {
            classTypeBuilder.addTypeVariables(superTypeVariables)
        }

        mtVariantObjectSpecs.forEach { mtObjectSpec ->
            val objectClass = createObjectSpec(superClassName, superTypeVariables, mtObjectSpec)
            classTypeBuilder.addType(objectClass)
        }

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addBaseObjectParseFunction(superClassName, superTypeVariables, mtVariantObjectSpecs)
            .build()

        return classTypeBuilder
            .addType(companionObjectBuilder)
            .build()
    }

    private fun createObjectSpec(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpec: MTObjectSpec
    ): TypeSpec {
        val superInterfaceName = if (!superTypeVariables.isNullOrEmpty()) {
            superClassName.parameterizedBy(superTypeVariables)
        } else superClassName

        val className = typeNameFactory.createClassName(mtVariantObjectSpec.name, superClassName)
        val classBuilder = TypeSpec.classBuilder(className)
            .addSuperinterface(superInterfaceName)

        val typeVariables = mtVariantObjectSpec.genericVariables
            ?.values
            ?.map(typeNameFactory::createTypeVariableName)

        if (!typeVariables.isNullOrEmpty()) {
            classBuilder.addTypeVariables(typeVariables)
        }

        val objectProperties = mtVariantObjectSpec.propertiesSpecs?.map { mtPropertySpec ->
            propertySpecFactory.createPropertySpec(mtPropertySpec)
        }

        if (!objectProperties.isNullOrEmpty()) classBuilder
            .addPrimaryConstructor(objectProperties)
            .addModifiers(KModifier.DATA)

        val hashConstant = createConstantPropertySpec(
            mtVariantObjectSpec::constructorHash.name,
            mtVariantObjectSpec.constructorHash
        )

        val hashPropertySpec = PropertySpec.builder(MTObjectSpec::constructorHash.name, Int::class)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%L", hashConstant.name)
            .build()

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addProperty(hashConstant)
            .addObjectParseFunction(
                mtVariantObjectSpec,
                objectProperties,
                typeVariables,
                className
            ).build()

        val objectSerializeFunction = createObjectSerializeFunctionSpec(mtVariantObjectSpec)
        return classBuilder.addType(companionObjectBuilder)
            .addProperty(hashPropertySpec)
            .addFunction(objectSerializeFunction)
            .addKdoc(mtVariantObjectSpec.rawScheme)
            .build()
    }

    private fun TypeSpec.Builder.addBaseObjectParseFunction(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): TypeSpec.Builder = this.apply {
        val hashParameterName = MTObject::constructorHash.name
        val hashConstantName = camelToSnakeCase(hashParameterName).uppercase()

        val functionBuilder = MTMethod<*>::parse.asFun3Builder(
            superTypeVariables,
            superClassName
        )

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
            TYPE_CONCAT_INDICATOR,
            PARAMETER_OPEN_PARENTHESIS,
            superClassName.simpleName,
            CLASS_ACCESS_KEY,
            CLASS_KEYWORD,
            PARAMETER_SEPARATOR,
            hashParameterName,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        functionBuilder.addStatement(throwStatement, MTObjectParseException::class.asTypeName())
        val functionSpec = functionBuilder.endControlFlow().build()
        addFunction(functionSpec)
    }

    private fun TypeSpec.Builder.addObjectParseFunction(
        mtObjectSpec: MTObjectSpec,
        propertySpecs: List<PropertySpec>?,
        typeVariables: List<TypeVariableName>?,
        returnType: ClassName
    ): TypeSpec.Builder = this.apply {
        val hashParameterName = MTObject::constructorHash.name
        val hashConstantName = camelToSnakeCase(hashParameterName).uppercase()

        val hashValidationStatement = StringBuilder().append(
            REQUIRE_METHOD,
            PARAMETER_OPEN_PARENTHESIS,
            hashParameterName,
            EQUAL_SIGN,
            hashConstantName,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        val functionBuilder = MTMethod<*>::parse.asFun3Builder(typeVariables, returnType)
            .addStatement(hashValidationStatement)

        if (mtObjectSpec.hasFlags) {
            functionBuilder.addLocalPropertyParseStatement(FLAGS_PROPERTY_NAME, Int::class)
        }

        mtObjectSpec.propertiesSpecs?.forEach { mtPropertySpec ->
            functionBuilder.addPropertyParseStatement(
                mtPropertySpec.name,
                mtPropertySpec.typeSpec,
                mtPropertySpec.flag
            )
        }

        val functionSpec = functionBuilder.addReturnConstructorStatement(
            returnType,
            propertySpecs,
            typeVariables
        ).build()

        addFunction(functionSpec)
    }

    private fun createObjectSerializeFunctionSpec(
        mtVariantObjectSpec: MTObjectSpec
    ): FunSpec {
        val functionBuilder = FunSpec.builder(MTObject::serialize.name)
            .addParameter(OUTPUT_STREAM_NAME, MTOutputStream::class)
            .addModifiers(KModifier.OVERRIDE)
            .addLocalPropertySerializeStatement(MTObject::constructorHash.name, Int::class)

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

        val flagPosition = DEFAULT_FLAG_BASE.pow(flag).toInt()
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
            MTTypeSpec.Flag -> { /* Do not serialize to stream */}
            MTTypeSpec.Type -> addGenericSerializeStatement(name, flag)
        }
    }

    private fun FunSpec.Builder.addGenericSerializeStatement(
        name: String,
        flag: Int?
    ): FunSpec.Builder = this.apply {
        val serializeStatement = StringBuilder().append(
            TYPE_CONCAT_INDICATOR,
            INSTANCE_ACCESS_KEY,
            MTObject::serialize.name,
            PARAMETER_OPEN_PARENTHESIS,
            OUTPUT_STREAM_NAME,
            PARAMETER_SEPARATOR,
            name,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        addSerializeFlagCheckStatement(name, flag) {
            addStatement(serializeStatement, SerializationHelper.javaClass.asTypeName())
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
            is MTTypeSpec.Generic.Variable -> addGenericParseStatement(name, typeSpec, flag)
            is MTTypeSpec.Primitive -> addLocalPropertyParseStatement(name, typeSpec.clazz, flag)
            is MTTypeSpec.Structure.Collection -> addCollectionParseStatement(name, typeSpec.elementGeneric, flag)
            MTTypeSpec.Flag -> addFlagParseStatement(name, requireNotNull(flag))
            MTTypeSpec.Type ->  { /* Can't parse property without type info */ }
        }
    }

    private fun FunSpec.Builder.addGenericParseStatement(
        name: String,
        typeSpec: MTTypeSpec.Generic.Variable,
        flag: Int?
    ): FunSpec.Builder = this.apply {
        val parseStatement = StringBuilder().append(
            TYPE_CONCAT_INDICATOR,
            INSTANCE_ACCESS_KEY,
            MTMethod<*>::parse.name,
            PARAMETER_OPEN_PARENTHESIS,
            INPUT_STREAM_NAME,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        val typeName = typeNameFactory.createTypeVariableName(typeSpec)
        addPropertyParseStatement(
            name,
            parseStatement,
            flag,
            typeName,
            SerializationHelper.javaClass.asTypeName()
        )
    }

    private fun FunSpec.Builder.addFlagParseStatement(
        name: String,
        position: Int
    ): FunSpec.Builder = this.apply {
        val flagPosition = DEFAULT_FLAG_BASE.pow(position).toInt()
        val flagCheckStatement = StringBuilder().append(
            PARAMETER_OPEN_PARENTHESIS,
            FLAGS_PROPERTY_NAME,
            AND_KEYWORD,
            flagPosition,
            PARAMETER_CLOSE_PARENTHESIS,
            NOT_EQUAL_SIGN,
            DEFAULT_FLAG_VALUE
        ).toString()

        val initializationStatement = StringBuilder().append(
            CONSTANT_KEYWORD,
            KEYWORD_SEPARATOR,
            name,
            TYPE_SIGN,
            KEYWORD_SEPARATOR,
            TYPE_CONCAT_INDICATOR,
            INITIALIZATION_SIGN,
            flagCheckStatement
        ).toString()

        addStatement(initializationStatement, Boolean::class)
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
        val genericTypes = typeSpec.generics?.map { generic ->
            typeNameFactory.createTypeName(generic)
        }

        val objectClassName = typeNameFactory.createClassName(typeSpec)
        val hashParseStatement = getParseStatement(Int::class)

        val objectParseStatement = createFunctionCallStatement(
            objectClassName.simpleName,
            MTMethod<*>::parse.name,
            INPUT_STREAM_NAME,
            hashParseStatement
        )

        val objectTypeName = if (!genericTypes.isNullOrEmpty()) {
            objectClassName.parameterizedBy(genericTypes)
        } else objectClassName

        addPropertyParseStatement(
            name,
            objectParseStatement,
            flag,
            objectTypeName
        )
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
        val sizeFieldPostfix = camelToTitleCase(List<*>::size.name)
        val sizeFieldName = StringBuilder().append(
            name,
            sizeFieldPostfix
        ).toString()

        addLocalPropertyParseStatement(sizeFieldName, Int::class)

        val arrayInitializeStatement = buildString {
            append(
                CONSTANT_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                INITIALIZATION_SIGN,
                TYPE_CONCAT_INDICATOR,
                PARAMETER_OPEN_PARENTHESIS,
                sizeFieldName,
                PARAMETER_CLOSE_PARENTHESIS
            )
        }

        val genericTypeName = typeNameFactory.createTypeName(elementGeneric)
        val arrayTypeName = ArrayList::class.asTypeName().parameterizedBy(genericTypeName)
        addStatement(arrayInitializeStatement, arrayTypeName)

        val collectionSizeName = StringBuilder().append(
            name,
            INSTANCE_ACCESS_KEY,
            List<*>::size.name
        ).toString()

        val iterationStatement = StringBuilder().append(
            WHILE_KEYWORD,
            PARAMETER_OPEN_PARENTHESIS,
            collectionSizeName,
            LESS_THAN_SIGN,
            sizeFieldName,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()

        beginControlFlow(iterationStatement)
        addPropertyParseStatement(ARRAY_ELEMENT_NAME, elementGeneric)

        val arrayAddStatement = buildString {
            append(
                name,
                INSTANCE_ACCESS_KEY,
                "add",
                PARAMETER_OPEN_PARENTHESIS,
                ARRAY_ELEMENT_NAME,
                PARAMETER_CLOSE_PARENTHESIS
            )
        }

        addStatement(arrayAddStatement)
        endControlFlow()
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

    private fun FunSpec.Builder.addLocalPropertyParseStatement(
        name: String,
        type: KClass<*>,
        flag: Int? = null
    ): FunSpec.Builder = this.apply {
        val parseMethod = getTypeParseMethod(type)
        val parseStatement = createFunctionCallStatement(INPUT_STREAM_NAME, parseMethod.name)

        addPropertyParseStatement(
            name,
            parseStatement,
            flag,
            type.asClassName()
        )
    }

    private fun getParseStatement(type: KClass<*>): String {
        val parseMethod = getTypeParseMethod(type)

        return StringBuilder().append(
            INPUT_STREAM_NAME,
            INSTANCE_ACCESS_KEY,
            parseMethod.name,
            PARAMETER_OPEN_PARENTHESIS,
            PARAMETER_CLOSE_PARENTHESIS
        ).toString()
    }

    private fun FunSpec.Builder.addPropertyParseStatement(
        name: String,
        parseStatement: String,
        flag: Int? = null,
        vararg typeName: TypeName
    ): FunSpec.Builder = this.apply {
        if (flag != null) {
            val definitionStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                TYPE_SIGN,
                KEYWORD_SEPARATOR,
                TYPE_CONCAT_INDICATOR,
                NULLABLE_SIGN,
                INITIALIZATION_SIGN,
                NULL_KEYWORD
            ).toString()

            addStatement(definitionStatement, *typeName)

            val flagPosition = DEFAULT_FLAG_BASE.pow(flag).toInt()
            val flagCheckStatement = StringBuilder().append(
                IF_KEYWORD,
                PARAMETER_OPEN_PARENTHESIS,
                PARAMETER_OPEN_PARENTHESIS,
                FLAGS_PROPERTY_NAME,
                AND_KEYWORD,
                flagPosition,
                PARAMETER_CLOSE_PARENTHESIS,
                NOT_EQUAL_SIGN,
                DEFAULT_FLAG_VALUE,
                PARAMETER_CLOSE_PARENTHESIS
            ).toString()

            beginControlFlow(flagCheckStatement)
            val assignStatement = StringBuilder().append(
                name,
                INITIALIZATION_SIGN,
                parseStatement
            ).toString()

            addStatement(assignStatement, *typeName)
            endControlFlow()
        } else {
            val initializationStatement = StringBuilder().append(
                CONSTANT_KEYWORD,
                KEYWORD_SEPARATOR,
                name,
                TYPE_SIGN,
                KEYWORD_SEPARATOR,
                TYPE_CONCAT_INDICATOR,
                INITIALIZATION_SIGN,
                parseStatement
            ).toString()

            addStatement(initializationStatement, *typeName)
        }
    }
}