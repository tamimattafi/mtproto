package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.*
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import com.squareup.kotlinpoet.*

object TLContainerFactory {

    fun createFileSpec(
        tlContainerSpec: TLContainerSpec,
        typeNameFactory: TypeNameFactory
    ): FileSpec {
        val className = typeNameFactory.createClassName(tlContainerSpec.superType)

        val classTypeSpec = createContainerSpec(
            className,
            tlContainerSpec,
            typeNameFactory
        )

        return FileSpec.builder(className.packageName, className.simpleName)
            .addType(classTypeSpec)
            .build()
    }

    private fun createContainerSpec(
        superClassName: ClassName,
        tlContainerSpec: TLContainerSpec,
        typeNameFactory: TypeNameFactory
    ): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(superClassName)
            .addSuperinterface(TLContainer::class)

        val typeVariables = tlContainerSpec.genericVariables
            ?.values
            ?.map(typeNameFactory::createTypeVariableName)

        if (!typeVariables.isNullOrEmpty()) {
            classBuilder.addTypeVariables(typeVariables)
        }

        val objectProperties = tlContainerSpec.propertiesSpecs?.map { mtPropertySpec ->
            TLPropertyFactory.createPropertySpec(mtPropertySpec, typeNameFactory)
        }

        if (!objectProperties.isNullOrEmpty()) classBuilder
            .addPrimaryConstructor(objectProperties)
            .addModifiers(KModifier.DATA)

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addObjectParseFunction(
                tlContainerSpec.hasFlags,
                tlContainerSpec.propertiesSpecs,
                objectProperties,
                typeVariables,
                superClassName,
                typeNameFactory
            ).build()

        val objectSerializeFunction = createObjectSerializeFunctionSpec(
            tlContainerSpec.hasFlags,
            tlContainerSpec.propertiesSpecs
        )

        return classBuilder.addType(companionObjectBuilder)
            .addFunction(objectSerializeFunction)
            .addKdoc(tlContainerSpec.rawScheme)
            .build()
    }

    private fun TypeSpec.Builder.addObjectParseFunction(
        hasFlags: Boolean,
        propertiesSpecs: List<TLPropertySpec>?,
        propertySpecs: List<PropertySpec>?,
        typeVariables: List<TypeVariableName>?,
        returnType: TypeName,
        typeNameFactory: TypeNameFactory
    ): TypeSpec.Builder = this.apply {
        val functionBuilder = TLMethod<*>::parse.asFun2Builder(typeVariables, returnType)

        if (hasFlags) {
            functionBuilder.addLocalPropertyParseStatement(FLAGS_PROPERTY_NAME, Int::class)
        }

        typeVariables?.forEach { typeVariableName ->
            functionBuilder.addClassInitializationStatement(typeVariableName)
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
    ): FunSpec {
        val functionBuilder = FunSpec.builder(TLObject::serialize.name)
            .addParameter(OUTPUT_STREAM_NAME, TLOutputStream::class)
            .addModifiers(KModifier.OVERRIDE)

        if (hasFlags) {
            val flagsInitializationStatement = StringBuilder().append(
                VARIABLE_KEYWORD,
                KEYWORD_SEPARATOR,
                FLAGS_PROPERTY_NAME,
                INITIALIZATION_SIGN,
                FLAGS_DEFAULT_VALUE
            ).toString()

            functionBuilder.addStatement(flagsInitializationStatement)
            propertiesSpecs?.forEach { mtPropertySpec ->
                if (mtPropertySpec.flag != null) functionBuilder.addFlaggingStatement(
                    mtPropertySpec,
                    mtPropertySpec.flag
                )
            }

            functionBuilder.addLocalPropertySerializeStatement(FLAGS_PROPERTY_NAME, Int::class)
        }

        propertiesSpecs?.forEach { tlPropertySpecs ->
            functionBuilder.addPropertySerializeStatement(
                tlPropertySpecs.name,
                tlPropertySpecs.typeSpec,
                tlPropertySpecs.flag
            )
        }

        return functionBuilder.build()
    }
}