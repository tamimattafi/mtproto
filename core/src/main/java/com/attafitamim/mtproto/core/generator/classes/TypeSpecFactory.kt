package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.generator.specs.MTObjectSpec
import com.squareup.kotlinpoet.TypeSpec

class TypeSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val propertySpecFactory: PropertySpecFactory
) {


    fun createObjectSpec(mtObjectSpec: MTObjectSpec): TypeSpec {

    }

    fun createMethodSpec(mtObjectSpec: MTObjectSpec): TypeSpec {

    }
}