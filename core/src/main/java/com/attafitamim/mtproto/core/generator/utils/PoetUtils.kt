package com.attafitamim.mtproto.core.generator.utils

import com.attafitamim.mtproto.core.generator.syntax.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction3
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

fun TypeSpec.Builder.addPrimaryConstructor(properties: List<PropertySpec>): TypeSpec.Builder {
    val propertySpecs = properties.map { property ->
        property.toBuilder().initializer(property.name).build()
    }

    val parameters = propertySpecs.map { propertySpec ->
        ParameterSpec.builder(propertySpec.name, propertySpec.type).build()
    }

    val constructor = FunSpec.constructorBuilder()
        .addParameters(parameters)
        .build()

    return this.primaryConstructor(constructor)
        .addProperties(propertySpecs)
}

fun createConstantPropertySpec(name: String, value: Any): PropertySpec {
    val constantName = camelToSnakeCase(name).uppercase(Locale.ROOT)
    return PropertySpec.builder(constantName, value::class)
        .mutable(false)
        .addModifiers(KModifier.CONST)
        .initializer("%L", value)
        .build()
}

fun KParameter.asParameterSpec() = ParameterSpec.builder(
    name.orEmpty(),
    type.javaType
).build()

fun Collection<KParameter>.asParameterSpecs() = map { kParameter ->
    kParameter.asParameterSpec()
}

fun KFunction3<*, *, *, *>.asFun3Builder(
    superTypeVariables: List<TypeName>?,
    returnType: ClassName
): FunSpec.Builder {
   val builder = FunSpec.builder(name)
       .addParameters(valueParameters.asParameterSpecs())

    val actualReturnType = if (!superTypeVariables.isNullOrEmpty()) {
        superTypeVariables.forEach { typeName ->
            if (typeName is TypeVariableName) {
                builder.addTypeVariable(typeName)
            }
        }

        returnType.parameterizedBy(superTypeVariables)
    } else returnType

    return builder.returns(actualReturnType)
}

fun KFunction1<*, *>.asFun1Builder(): FunSpec.Builder
    = FunSpec.builder(name)
        .addParameters(valueParameters.asParameterSpecs())
        .returns(returnType.asTypeName())

fun createFunctionCallStatement(
    parentName: String,
    functionName: String,
    vararg parameters: String
): String {
    val builder = StringBuilder()
        .append(
            parentName,
            INSTANCE_ACCESS_KEY,
            functionName,
            PARAMETER_OPEN_PARENTHESIS
        )

    if (!parameters.isNullOrEmpty()) {
        for (index in 0 until parameters.lastIndex) builder.append(
            parameters[index],
            PARAMETER_SEPARATOR
        )

        builder.append(parameters.last())
    }

    return builder.append(PARAMETER_CLOSE_PARENTHESIS)
        .toString()
}

fun createCostructorCallStatement(properties: List<String>?): String {
    val builder = StringBuilder()
        .append(TYPE_CONCAT_INDICATOR, PARAMETER_OPEN_PARENTHESIS)

    if (!properties.isNullOrEmpty()) {
        for (index in 0 until properties.lastIndex) builder.append(
            properties[index],
            PARAMETER_SEPARATOR
        )

        builder.append(properties.last())
    }

    return builder.append(PARAMETER_CLOSE_PARENTHESIS)
        .toString()
}

fun FunSpec.Builder.addReturnStatement(
    statement: String,
    vararg typeName: TypeName
): FunSpec.Builder {
    val returnStatement = StringBuilder()
        .append(RETURN_KEYWORD, KEYWORD_SEPARATOR, statement)
        .toString()

    return addStatement(returnStatement, *typeName)
}

fun FunSpec.Builder.addReturnConstructorStatement(
    className: ClassName,
    properties: List<PropertySpec>?,
    typeVariables: List<TypeVariableName>?
): FunSpec.Builder {
    val propertyNames = properties?.map(PropertySpec::name)
    val statement = createCostructorCallStatement(propertyNames)
    val actualClassName = if (!typeVariables.isNullOrEmpty()) {
        className.parameterizedBy(typeVariables)
    } else className

    return addReturnStatement(statement, actualClassName)
}