package com.attafitamim.mtproto.core.generator.poet.factories

import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.camelToTitleCase
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase
import com.attafitamim.mtproto.core.types.TLContainer
import com.attafitamim.mtproto.core.types.TLObject
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class TypeNameFactory(private val basePackage: String) {

    fun createTypeName(mtTypeSpec: TLTypeSpec): TypeName
        = when(mtTypeSpec) {
            is TLTypeSpec.Generic -> mtTypeSpec.toTypeName()
            is TLTypeSpec.Primitive -> mtTypeSpec.clazz.asTypeName()
            is TLTypeSpec.TLType.Object -> mtTypeSpec.toTypeName()
            is TLTypeSpec.TLType.Container -> mtTypeSpec.toTypeName()
            TLTypeSpec.TLType.SuperContainer -> TLContainer::class.asTypeName()
            TLTypeSpec.TLType.SuperObject -> TLObject::class.asTypeName()
            is TLTypeSpec.Structure -> mtTypeSpec.toTypeName()
            TLTypeSpec.Type -> Any::class.asTypeName()
            TLTypeSpec.Flag -> Boolean::class.asTypeName()
    }

    fun createClassName(name: String, superClassName: ClassName): ClassName {
        val formattedClassName = name
            .let(::camelToTitleCase)

        val classNames = listOf(superClassName.simpleName, formattedClassName)
        return ClassName(superClassName.packageName, classNames)
    }

    fun createClassName(mtSuperObjectSpec: TLTypeSpec.TLType): ClassName =
        when(mtSuperObjectSpec) {
            is TLTypeSpec.TLType.Container -> createClassName(
                mtSuperObjectSpec.name,
                CONTAINERS_FOLDER_NAME,
                mtSuperObjectSpec.namespace
            )

            is TLTypeSpec.TLType.Object -> createClassName(
                mtSuperObjectSpec.name,
                TYPES_FOLDER_NAME,
                mtSuperObjectSpec.namespace
            )

            TLTypeSpec.TLType.SuperContainer -> TLContainer::class.asClassName()
            TLTypeSpec.TLType.SuperObject -> TLObject::class.asClassName()
        }

    fun createTypeVariableName(genericVariable: TLTypeSpec.Generic.Variable): TypeVariableName {
        val typeBound = createTypeName(genericVariable.superType)
        return TypeVariableName.invoke(genericVariable.name, typeBound)
    }

    fun createMethodClassName(name: String, namespace: String? = null) =
        createClassName(name, METHODS_FOLDER_NAME, namespace)

    private fun createClassName(
        name: String,
        folder: String,
        namespace: String? = null
    ): ClassName {
        val formattedNameSpace = namespace?.let(::snakeToTitleCase)
            .orEmpty()

        val formattedClassName = snakeToTitleCase(name)

        val finalClassName = StringBuilder()
            .append(TYPES_PREFIX)
            .append(formattedNameSpace)
            .append(formattedClassName)
            .toString()

        val packageName = createPackageName(folder, namespace)
        return ClassName(packageName, finalClassName)
    }

    private fun createPackageName(
        folder: String,
        namespace: String?
    ): String {
        val actualNameSpace = namespace ?: GLOBAL_NAMESPACE
        return buildString {
            append(
                basePackage,
                PACKAGE_SEPARATOR,
                folder,
                PACKAGE_SEPARATOR,
                actualNameSpace
            )
        }
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
        val className = createClassName(
            name,
            TYPES_FOLDER_NAME,
            namespace
        )

        val genericParameters = generics?.map { genericTypeSpec ->
            genericTypeSpec.toTypeName()
        }

        return if (!genericParameters.isNullOrEmpty()) {
            className.parameterizedBy(genericParameters)
        } else className
    }

    private fun TLTypeSpec.TLType.Container.toTypeName(): TypeName {
        val className = createClassName(
            name,
            CONTAINERS_FOLDER_NAME,
            namespace
        )

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
