package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase
import com.squareup.kotlinpoet.PropertySpec

object TLPropertyFactory {

    fun createPropertySpec(
        mtPropertySpec: TLPropertySpec,
        typeNameFactory: TypeNameFactory
    ): PropertySpec {
        val formattedName = snakeToCamelCase(mtPropertySpec.name)
        var propertyType = typeNameFactory.createTypeName(mtPropertySpec.typeSpec)

        if (mtPropertySpec.typeSpec != TLTypeSpec.Flag) {
            propertyType = propertyType.copy(mtPropertySpec.flag != null)
        }

        return PropertySpec.builder(formattedName, propertyType).build()
    }
}
