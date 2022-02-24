package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.generator.constants.GLOBAL_NAMESPACE
import com.attafitamim.mtproto.core.generator.constants.PACKAGE_SEPARATOR
import com.attafitamim.mtproto.core.generator.constants.TYPES_FOLDER_NAME
import com.attafitamim.mtproto.core.generator.constants.TYPES_PREFIX
import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.utils.camelToTitleCase
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase
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

    fun createClassName(mtObjectSpec: MTTypeSpec.Object): ClassName {
        val formattedNameSpace = mtObjectSpec.namespace
            ?.let(::snakeToTitleCase)
            .orEmpty()

        val formattedClassName = snakeToTitleCase(mtObjectSpec.name)

        val finalClassName = StringBuilder()
            .append(TYPES_PREFIX)
            .append(formattedNameSpace)
            .append(formattedClassName)
            .toString()

        val namespace = mtObjectSpec.namespace ?: GLOBAL_NAMESPACE
        val packageName = StringBuilder(basePackage)
            .append(PACKAGE_SEPARATOR)
            .append(TYPES_FOLDER_NAME)
            .append(PACKAGE_SEPARATOR)
            .append(namespace)
            .toString()

        return ClassName(packageName, finalClassName)
    }

    fun createClassName(name: String, superClassName: ClassName): ClassName {
        val formattedClassName = name
            .let(::snakeToCamelCase)
            .let(::camelToTitleCase)

        val classNames = listOf(superClassName.simpleName, formattedClassName)
        return ClassName(superClassName.packageName, classNames)
    }

    fun createClassName(name: String, mtSuperObjectSpec: MTTypeSpec.Object): ClassName {
        val superClassName = createClassName(mtSuperObjectSpec)
        return createClassName(name, superClassName)
    }

    private fun MTTypeSpec.Structure.toTypeName(): TypeName = when(this) {
        is MTTypeSpec.Structure.Collection -> toTypeName()
    }

    private fun MTTypeSpec.Structure.Collection.toTypeName(): TypeName {
        val className = clazz.asTypeName()
        return if (elementGeneric != null) {
            val genericParameter = elementGeneric.toTypeName()
            className.parameterizedBy(genericParameter)
        } else className
    }

    private fun MTTypeSpec.Object.toTypeName(): TypeName {
        val className = createClassName(this)
        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun MTTypeSpec.Generic.toTypeName(): TypeName = when(this) {
        is MTTypeSpec.Generic.Variable -> toTypeName()
        is MTTypeSpec.Generic.Parameter -> toTypeName()
    }

    private fun MTTypeSpec.Generic.Variable.toTypeName(): TypeName {
        val typeBound = createTypeName(superType)
        return TypeVariableName.invoke(name, typeBound)
    }

    private fun MTTypeSpec.Generic.Parameter.toTypeName(): TypeName {
        return createTypeName(type)
    }
}