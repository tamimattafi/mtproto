package com.attafitamim.mtproto.core.generator.classes

import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class TypeNameFactory(private val basePackage: String) {

    private companion object {
        const val GLOBAL_NAMESPACE = "global"
        const val TYPES_PREFIX = "MT"
    }

    fun createTypeName(mtTypeSpec: MTTypeSpec): TypeName
        = when(mtTypeSpec) {
            is MTTypeSpec.Generic -> mtTypeSpec.toTypeName()
            is MTTypeSpec.Local -> mtTypeSpec.clazz.asTypeName()
            is MTTypeSpec.Object -> mtTypeSpec.toTypeName()
            is MTTypeSpec.Structure -> mtTypeSpec.toTypeName()
        }

    fun createClassName(mtObjectSpec: MTTypeSpec.Object): ClassName {
        val formattedNameSpace = mtObjectSpec.namespace
            ?.let(TextUtils::snakeToCamelCase)
            ?.let(TextUtils::camelToTitleCase)
            .orEmpty()

        val formattedClassName = mtObjectSpec.name
            .let(TextUtils::snakeToCamelCase)
            .let(TextUtils::camelToTitleCase)

        val finalClassName = StringBuilder()
            .append(TYPES_PREFIX)
            .append(formattedNameSpace)
            .append(formattedClassName)
            .toString()

        val namespace = mtObjectSpec.namespace ?: GLOBAL_NAMESPACE
        val packageName = StringBuilder(basePackage)
            .append(Constants.PACKAGE_SEPARATOR)
            .append(Constants.TYPES_FOLDER_NAME)
            .append(Constants.PACKAGE_SEPARATOR)
            .append(namespace)
            .toString()

        return ClassName(packageName, finalClassName)
    }

    fun createClassName(name: String, superClassName: ClassName): ClassName {
        val formattedClassName = name
            .let(TextUtils::snakeToCamelCase)
            .let(TextUtils::camelToTitleCase)

        val classNames = listOf(superClassName.simpleName, formattedClassName)
        return ClassName(superClassName.packageName, classNames)
    }

    fun createClassName(name: String, mtSuperObjectSpec: MTTypeSpec.Object): ClassName {
        val superClassName = createClassName(mtSuperObjectSpec)
        return createClassName(name, superClassName)
    }

    private fun MTTypeSpec.Generic.toTypeName(): TypeName = when(this) {
        is MTTypeSpec.Generic.Variable -> toTypeName()
        is MTTypeSpec.Generic.Parameter -> toTypeName()
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