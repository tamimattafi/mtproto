package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class TypeNameFactory(private val basePackage: String) {

    fun createTypeName(mtTypeSpec: MTTypeSpec): TypeName
        = when(mtTypeSpec) {
            is MTTypeSpec.Generic -> mtTypeSpec.toTypeName()
            is MTTypeSpec.Local -> mtTypeSpec.clazz.asTypeName()
            is MTTypeSpec.Object -> mtTypeSpec.toTypeName()
            is MTTypeSpec.Structure -> mtTypeSpec.toTypeName()
        }

    private fun MTTypeSpec.Generic.toTypeName(): TypeName = when(this) {
        is MTTypeSpec.Generic.Variable -> toTypeName()
        is MTTypeSpec.Generic.Parameter -> toTypeName()
    }

    private fun MTTypeSpec.Object.toTypeName(): TypeName {
        val packageBuilder = StringBuilder(basePackage)
            .append(Constants.PACKAGE_SEPARATOR)
            .append(Constants.TYPES_FOLDER_NAME)

        if (!namespace.isNullOrBlank()) packageBuilder
            .append(Constants.PACKAGE_SEPARATOR)
            .append(name)

        val packageName = packageBuilder.toString()
        val className = ClassName(packageName, name)
        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun MTTypeSpec.Structure.toTypeName(): TypeName {
        val className = clazz.asTypeName()
        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun MTTypeSpec.Generic.Variable.toTypeName(): TypeName {
        val typeBound = createTypeName(superType)
        return TypeVariableName.invoke(name, typeBound)
    }

    private fun MTTypeSpec.Generic.Parameter.toTypeName(): TypeName {
        return createTypeName(type)
    }
}