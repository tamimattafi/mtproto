package com.attafitamim.mtproto.core.generator.utils

import com.squareup.kotlinpoet.*
import java.util.*
import kotlin.reflect.KClass

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
    val constantName = name.uppercase(Locale.ROOT)
    return PropertySpec.builder(constantName, value::class)
        .mutable(false)
        .addModifiers(KModifier.CONST)
        .initializer("%L", value)
        .build()
}