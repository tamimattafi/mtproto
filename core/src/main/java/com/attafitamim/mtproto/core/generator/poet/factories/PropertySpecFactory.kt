package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase
import com.squareup.kotlinpoet.PropertySpec

class PropertySpecFactory(private val typeNameFactory: TypeNameFactory) {

    fun createPropertySpec(mtPropertySpec: MTPropertySpec): PropertySpec {
        val formattedName = snakeToCamelCase(mtPropertySpec.name)
        val propertyType = typeNameFactory.createTypeName(mtPropertySpec.typeSpec)
            .copy(mtPropertySpec.flag != null)

        return PropertySpec.builder(formattedName, propertyType).build()
    }
}