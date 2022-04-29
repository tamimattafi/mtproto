package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.squareup.kotlinpoet.FileSpec

class FileSpecFactory(
    private val typeNameFactory: TypeNameFactory,
    private val typeSpecFactory: TypeSpecFactory
) {

    fun createFileSpec(
        superClassName: TLTypeSpec.TLType.Object,
        mtVariantObjectSpecs: List<TLObjectSpec>
    ): FileSpec {
        val className = typeNameFactory.createClassName(superClassName)
        val typeVariables = superClassName.generics?.mapNotNull { generic ->
            when(generic) {
                is TLTypeSpec.Generic.Variable -> typeNameFactory.createTypeVariableName(generic)
                is TLTypeSpec.Generic.Parameter -> null
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

    fun createFileSpec(
        tlContainerSpec: TLContainerSpec
    ): FileSpec {
        val className = typeNameFactory.createClassName(tlContainerSpec.superType)

        val classTypeSpec = typeSpecFactory.createContainerSpec(
            className,
            tlContainerSpec
        )

        return FileSpec.builder(className.packageName, className.simpleName)
            .addType(classTypeSpec)
            .build()
    }
}