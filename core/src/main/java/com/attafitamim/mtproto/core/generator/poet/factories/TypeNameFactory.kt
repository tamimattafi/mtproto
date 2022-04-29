package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.GLOBAL_NAMESPACE
import com.attafitamim.mtproto.core.generator.syntax.PACKAGE_SEPARATOR
import com.attafitamim.mtproto.core.generator.syntax.TYPES_FOLDER_NAME
import com.attafitamim.mtproto.core.generator.syntax.TYPES_PREFIX
import com.attafitamim.mtproto.core.generator.utils.camelToTitleCase
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

class TypeNameFactory(private val basePackage: String) {

    fun createTypeName(mtTypeSpec: TLTypeSpec): TypeName
        = when(mtTypeSpec) {
            is TLTypeSpec.Generic -> mtTypeSpec.toTypeName()
            is TLTypeSpec.Primitive -> mtTypeSpec.clazz.asTypeName()
            is TLTypeSpec.TLType.Object -> mtTypeSpec.toTypeName()
            is TLTypeSpec.TLType.Container -> mtTypeSpec.toTypeName()
            is TLTypeSpec.Structure -> mtTypeSpec.toTypeName()
            TLTypeSpec.Type -> Any::class.asTypeName()
            TLTypeSpec.Flag -> Boolean::class.asTypeName()
    }

    fun createClassName(mtObjectSpec: TLTypeSpec.TLType.Object): ClassName {
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

    fun createClassName(mtObjectSpec: TLTypeSpec.TLType.Container): ClassName {
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
            .let(::camelToTitleCase)

        val classNames = listOf(superClassName.simpleName, formattedClassName)
        return ClassName(superClassName.packageName, classNames)
    }

    fun createClassName(name: String, mtSuperObjectSpec: TLTypeSpec.TLType.Object): ClassName {
        val superClassName = createClassName(mtSuperObjectSpec)
        return createClassName(name, superClassName)
    }

    fun createTypeVariableName(genericVariable: TLTypeSpec.Generic.Variable): TypeVariableName {
        val typeBound = createTypeName(genericVariable.superType)
        return TypeVariableName.invoke(genericVariable.name, typeBound)
    }

    private fun TLTypeSpec.Structure.toTypeName(): TypeName = when(this) {
        is TLTypeSpec.Structure.Collection -> toTypeName()
        is TLTypeSpec.Structure.Bytes -> ByteArray::class.asTypeName()
    }

    private fun TLTypeSpec.Structure.Collection.toTypeName(): TypeName {
        val genericParameter = elementGeneric.toTypeName()
        return clazz.asTypeName().parameterizedBy(genericParameter)
    }

    private fun TLTypeSpec.TLType.Object.toTypeName(): TypeName {
        val className = createClassName(this)
        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun TLTypeSpec.TLType.Container.toTypeName(): TypeName {
        val className = createClassName(this)
        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun TLTypeSpec.Generic.toTypeName(): TypeName = when(this) {
        is TLTypeSpec.Generic.Variable -> createTypeVariableName(this)
        is TLTypeSpec.Generic.Parameter -> toTypeName()
    }

    private fun TLTypeSpec.Generic.Parameter.toTypeName(): TypeName {
        return createTypeName(type)
    }
}