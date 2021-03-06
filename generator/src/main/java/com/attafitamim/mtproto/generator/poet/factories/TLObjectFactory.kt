package com.attafitamim.mtproto.generator.poet.factories

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.behavior.TLParser
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject
import com.attafitamim.mtproto.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.generator.syntax.*
import com.attafitamim.mtproto.generator.utils.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object TLObjectFactory {

    fun createFileSpec(
        superClassName: TLTypeSpec.TLType.Object,
        mtVariantObjectSpecs: List<TLObjectSpec>,
        typeNameFactory: TypeNameFactory
    ): FileSpec {
        val className = typeNameFactory.createClassName(superClassName)
        val typeVariables = superClassName.generics?.mapNotNull { generic ->
            when(generic) {
                is TLTypeSpec.Generic.Variable -> typeNameFactory.createTypeVariableName(generic)
                is TLTypeSpec.Generic.Parameter -> null
            }
        }

        val classTypeSpec = createObjectSpec(
            className,
            typeVariables,
            mtVariantObjectSpecs,
            typeNameFactory
        )

        return FileSpec.builder(className.packageName, className.simpleName)
            .addType(classTypeSpec)
            .build()
    }

    private fun createObjectSpec(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpecs: List<TLObjectSpec>,
        typeNameFactory: TypeNameFactory
    ): TypeSpec {
        val classTypeBuilder = TypeSpec.interfaceBuilder(superClassName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(TLObject::class)

        if (superTypeVariables != null) {
            classTypeBuilder.addTypeVariables(superTypeVariables)
        }

        mtVariantObjectSpecs.forEach { mtObjectSpecs ->
            val objectClass = createObjectSpec(
                superClassName,
                superTypeVariables,
                mtObjectSpecs,
                typeNameFactory
            )

            classTypeBuilder.addType(objectClass)
        }

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addBaseObjectParseFunction(
                superClassName,
                superTypeVariables,
                mtVariantObjectSpecs,
                typeNameFactory
            ).build()

        return classTypeBuilder
            .addType(companionObjectBuilder)
            .build()
    }


    private fun createObjectSpec(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpec: TLObjectSpec,
        typeNameFactory: TypeNameFactory
    ): TypeSpec {
        val hashInt = mtVariantObjectSpec.constructorHash.toLong(16)
            .toInt()

        val hashConstant = createConstantPropertySpec(
            mtVariantObjectSpec::constructorHash.name,
            hashInt
        )

        val objectProperties = mtVariantObjectSpec.propertiesSpecs?.map { mtPropertySpec ->
            TLPropertyFactory.createPropertySpec(mtPropertySpec, typeNameFactory)
        }

        val superInterfaceName = if (!superTypeVariables.isNullOrEmpty()) {
            superClassName.parameterizedBy(superTypeVariables)
        } else superClassName

        val genericTypeVariables = mtVariantObjectSpec.genericVariables
            ?.values
            ?.map(typeNameFactory::createTypeVariableName)

        val className = typeNameFactory.createClassName(mtVariantObjectSpec.name, superClassName)
        val classBuilder = if (!objectProperties.isNullOrEmpty()) {
            val companionObjectSpec = TypeSpec.companionObjectBuilder()
                .apply {
                    addProperty(hashConstant)

                    addObjectParseFunction(
                        mtVariantObjectSpec.hasFlags,
                        mtVariantObjectSpec.propertiesSpecs,
                        objectProperties,
                        genericTypeVariables,
                        className,
                        typeNameFactory
                    )
                }.build()

            TypeSpec.classBuilder(className)
                .addPrimaryConstructor(objectProperties)
                .addModifiers(KModifier.DATA)
                .addType(companionObjectSpec)
        } else {
            TypeSpec.objectBuilder(className).
                addProperty(hashConstant)
        }

        classBuilder.addSuperinterface(superInterfaceName)

        if (!genericTypeVariables.isNullOrEmpty()) {
            classBuilder.addTypeVariables(genericTypeVariables)
        }

        val hashPropertySpec = PropertySpec.builder(TLObjectSpec::constructorHash.name, Int::class)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%L", hashConstant.name)
            .build()

        val objectSerializeFunction = createObjectSerializeFunctionSpec(
            mtVariantObjectSpec.hasFlags,
            mtVariantObjectSpec.propertiesSpecs
        )

        return classBuilder.addProperty(hashPropertySpec)
            .addFunction(objectSerializeFunction)
            .addKdoc(mtVariantObjectSpec.rawScheme)
            .build()
    }

    private fun TypeSpec.Builder.addBaseObjectParseFunction(
        superClassName: ClassName,
        superTypeVariables: List<TypeVariableName>?,
        mtVariantObjectSpecs: List<TLObjectSpec>,
        typeNameFactory: TypeNameFactory
    ): TypeSpec.Builder = this.apply {
        val hashParameterName = TLObject::constructorHash.name
        val hashConstantName = camelToSnakeCase(hashParameterName).toUpperCase()

        val functionBuilder = TLParser<*>::parse.asParseFunctionBuilder(
            superTypeVariables,
            superClassName
        )

        functionBuilder.addLocalPropertyParseStatement(hashParameterName, Int::class)

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
            val statementBuilder = StringBuilder().append(
                objectClass.simpleName,
                INSTANCE_ACCESS_KEY,
                hashConstantName,
                RESULT_ARROW
            )

            if (!mtObjectSpec.propertiesSpecs.isNullOrEmpty()) {
                statementBuilder.append(
                    objectClass.simpleName,
                    INSTANCE_ACCESS_KEY,
                    TLParser<*>::parse.name,
                    PARAMETER_OPEN_PARENTHESIS,
                    INPUT_STREAM_NAME
                )

                mtObjectSpec.genericVariables?.values?.forEach { typeVariable ->
                    val parserName = createTypeParserParameterName(typeVariable.name)
                    statementBuilder.append(PARAMETER_SEPARATOR, parserName)
                }

                val parseStatement = statementBuilder.append(PARAMETER_CLOSE_PARENTHESIS).toString()
                functionBuilder.addStatement(parseStatement)
            } else {
                val parseStatement = statementBuilder.append(TYPE_CONCAT_INDICATOR).toString()
                functionBuilder.addStatement(parseStatement, objectClass)
            }
        }

        val throwStatement = StringBuilder().append(
            ELSE_KEYWORD,
            RESULT_ARROW,
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

        functionBuilder.addStatement(throwStatement, TLObjectParseException::class.asTypeName())
        val functionSpec = functionBuilder.endControlFlow().build()
        addFunction(functionSpec)
    }

    private fun TypeSpec.Builder.addObjectParseFunction(
        hasFlags: Boolean,
        propertiesSpecs: List<TLPropertySpec>?,
        propertySpecs: List<PropertySpec>?,
        typeVariables: List<TypeVariableName>?,
        returnType: TypeName,
        typeNameFactory: TypeNameFactory
    ): TypeSpec.Builder = this.apply {
        val functionBuilder = TLParser<*>::parse.asParseFunctionBuilder(typeVariables, returnType)

        if (hasFlags) {
            functionBuilder.addLocalPropertyParseStatement(FLAGS_PROPERTY_NAME, Int::class)
        }

        propertiesSpecs?.forEach { mtPropertySpec ->
            functionBuilder.addPropertyParseStatement(
                mtPropertySpec.name,
                mtPropertySpec.typeSpec,
                typeNameFactory,
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
        hasFlags: Boolean,
        propertiesSpecs: List<TLPropertySpec>?
    ): FunSpec = FunSpec.builder(TLSerializable::serialize.name).apply {
        addParameter(OUTPUT_STREAM_NAME, TLOutputStream::class)
        addModifiers(KModifier.OVERRIDE)
        addLocalPropertySerializeStatement(
            TLObject::constructorHash.name,
            Int::class
        )

        if (hasFlags) addFlaggingSerializationLogic(propertiesSpecs)
        addPropertiesSerializationLogic(propertiesSpecs)
    }.build()
}
