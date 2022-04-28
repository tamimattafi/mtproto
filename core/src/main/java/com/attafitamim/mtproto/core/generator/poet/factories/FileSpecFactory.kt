package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.MTObjectSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTTypeSpec
import com.squareup.kotlinpoet.FileSpec

class FileSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val typeSpecFactory: TypeSpecFactory
) {

    fun createFileSpec(
        superClassName: MTTypeSpec.Object,
        mtVariantObjectSpecs: List<MTObjectSpec>
    ): FileSpec {
        val className = typeNameFactory.createClassName(superClassName)
        val typeVariables = superClassName.generics?.mapNotNull { generic ->
            when(generic) {
                is MTTypeSpec.Generic.Variable -> typeNameFactory.createTypeVariableName(generic)
                is MTTypeSpec.Generic.Parameter -> null
            }
        }

        val classTypeSpec = typeSpecFactory.createObjectSpec(
            className,
            typeVariables,
            mtVariantObjectSpecs
        )

        return FileSpec.builder(className.packageName, className.simpleName)
            .addType(classTypeSpec)
            .build()
    }
}