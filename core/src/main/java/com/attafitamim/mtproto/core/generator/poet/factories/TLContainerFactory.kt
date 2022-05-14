package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLPropertySpec
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
        return FileSpec.builder(className.packageName, className.simpleName)
            .addContainerType(
                className,
                tlContainerSpec,
                typeNameFactory
            ).build()
    }

    private fun FileSpec.Builder.addContainerType(
        superClassName: ClassName,
        tlContainerSpec: TLContainerSpec,
        typeNameFactory: TypeNameFactory
    ): FileSpec.Builder {
        val objectProperties = tlContainerSpec.propertiesSpecs.map { mtPropertySpec ->
            TLPropertyFactory.createPropertySpec(mtPropertySpec, typeNameFactory)
        }

        val classBuilder = TypeSpec.classBuilder(superClassName)
            .addPrimaryConstructor(objectProperties)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(TLContainer::class)

        val typeVariables = tlContainerSpec.genericVariables
            ?.values
            ?.map(typeNameFactory::createTypeVariableName)

        if (!typeVariables.isNullOrEmpty()) {
            classBuilder.addTypeVariables(typeVariables)
        }

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

        val containerType = classBuilder.addType(companionObjectBuilder)
            .addFunction(objectSerializeFunction)
            .addKdoc(tlContainerSpec.rawScheme)
            .build()

        return addType(containerType)
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
    ): FunSpec = FunSpec.builder(TLObject::serialize.name).apply {
        addParameter(OUTPUT_STREAM_NAME, TLOutputStream::class)
        addModifiers(KModifier.OVERRIDE)

        if (hasFlags) addFlaggingSerializationLogic(propertiesSpecs)
        addPropertiesSerializationLogic(propertiesSpecs)
    }.build()
}
