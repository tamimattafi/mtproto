package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.generator.specs.MTPropertySpec
import com.squareup.kotlinpoet.PropertySpec

class PropertySpecFactory(
    private val typeNameFactory: TypeNameFactory
) {

    fun createPropertySpec(mtPropertySpec: MTPropertySpec): PropertySpec {
        val formattedName = TextUtils.snakeToCamelCase(mtPropertySpec.name)
        val propertyType = typeNameFactory.createTypeName(mtPropertySpec.typeSpec)
            .copy(mtPropertySpec.flag != null)

        return PropertySpec.builder(formattedName, propertyType).build()
    }
}