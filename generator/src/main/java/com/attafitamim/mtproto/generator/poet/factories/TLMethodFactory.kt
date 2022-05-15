package com.attafitamim.mtproto.generator.poet.factories

import com.attafitamim.mtproto.core.serialization.behavior.TLParser
import com.attafitamim.mtproto.core.serialization.behavior.TLSerializable
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod
import com.attafitamim.mtproto.core.types.TLObject
import com.attafitamim.mtproto.generator.scheme.specs.TLMethodSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.generator.syntax.*
import com.attafitamim.mtproto.generator.utils.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

object TLMethodFactory {

    fun createFileSpec(
        methodSpecs: TLMethodSpec,
        typeNameFactory: TypeNameFactory
    ): FileSpec {
        val className = typeNameFactory.createMethodClassName(methodSpecs.name, methodSpecs.namespace)
        return FileSpec.builder(className.packageName, className.simpleName)
            .addMethodType(methodSpecs, typeNameFactory)
            .build()
    }

    private fun FileSpec.Builder.addMethodType(
        tlMethodSpec: TLMethodSpec,
        typeNameFactory: TypeNameFactory
    ): FileSpec.Builder {
        val returnType = typeNameFactory.createTypeName(tlMethodSpec.returnType)
        val superInterface = TLMethod::class.asTypeName().parameterizedBy(returnType)
        val classBuilder = TypeSpec.classBuilder(tlMethodSpec.name)
            .addSuperinterface(superInterface)

        val typeVariables = tlMethodSpec.genericVariables
            ?.values
            ?.map(typeNameFactory::createTypeVariableName)

        if (!typeVariables.isNullOrEmpty()) {
            classBuilder.addTypeVariables(typeVariables)
        }

        val methodProperties = ArrayList<PropertySpec>()
        tlMethodSpec.propertiesSpecs?.forEach { mtPropertySpec ->
            val propertySpec = TLPropertyFactory.createPropertySpec(
                mtPropertySpec,
                typeNameFactory
            )

            methodProperties.add(propertySpec)
        }

        if (tlMethodSpec.returnType is TLTypeSpec.Generic.Variable) {
            val responseClass = KClass::class.asTypeName()
                .parameterizedBy(returnType)

            val className = createTypeVariableClassName(tlMethodSpec.returnType.name)
            val responseClassProperty = PropertySpec.builder(className, responseClass).build()

            methodProperties.add(responseClassProperty)
        }

        if (!methodProperties.isNullOrEmpty()) classBuilder
            .addPrimaryConstructor(methodProperties)
            .addModifiers(KModifier.DATA)

        val objectSerializeFunction = createMethodSerializationFunction(
            tlMethodSpec.hasFlags,
            tlMethodSpec.propertiesSpecs
        )

        val methodParseFunction = createMethodParseFunction(
            tlMethodSpec.returnType,
            typeNameFactory
        )

        val hashInt = tlMethodSpec.constructorHash.toLong(16)
            .toInt()

        val hashConstant = createConstantPropertySpec(
            tlMethodSpec::constructorHash.name,
            hashInt
        )

        val hashPropertySpec = PropertySpec.builder(TLObjectSpec::constructorHash.name, Int::class)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(LITERAL_CONCAT_INDICATOR, hashConstant.name)
            .build()

        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
            .addProperty(hashConstant)
            .build()

        val methodType = classBuilder.addFunction(objectSerializeFunction)
            .addType(companionObjectBuilder)
            .addProperty(hashPropertySpec)
            .addFunction(methodParseFunction)
            .addKdoc(tlMethodSpec.rawScheme)
            .build()

        return addType(methodType)
    }

    private fun createMethodParseFunction(
        returnType: TLTypeSpec,
        typeNameFactory: TypeNameFactory
    ): FunSpec {
        val returnTypeName = typeNameFactory.createTypeName(returnType)
        return TLParser<*>::parse.asFun2Builder(null, returnTypeName)
            .addModifiers(KModifier.OVERRIDE)
            .addPropertyParseStatement(METHOD_RESPONSE_NAME, returnType, typeNameFactory)
            .addReturnStatement(METHOD_RESPONSE_NAME)
            .build()
    }

    private fun createMethodSerializationFunction(
        hasFlags: Boolean,
        propertiesSpecs: List<TLPropertySpec>?
    ): FunSpec = FunSpec.builder(TLSerializable::serialize.name).apply {
        addParameter(OUTPUT_STREAM_NAME, TLOutputStream::class)
        addModifiers(KModifier.OVERRIDE)
        addLocalPropertySerializeStatement(
            TLMethod<*>::constructorHash.name,
            Int::class
        )

        if (hasFlags) addFlaggingSerializationLogic(propertiesSpecs)
        addPropertiesSerializationLogic(propertiesSpecs)
    }.build()
}
