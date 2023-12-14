package com.attafitamim.mtproto.generator.scheme.parsers

import com.attafitamim.mtproto.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.generator.syntax.ANY_TYPE_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.BOOLEAN_FLAG_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.BYTES_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.BYTE_ARRAY_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.GENERIC_PARAMETER_CLOSING_QUOTATION
import com.attafitamim.mtproto.generator.syntax.GENERIC_PARAMETER_OPENING_QUOTATION
import com.attafitamim.mtproto.generator.syntax.GENERIC_SEPARATOR
import com.attafitamim.mtproto.generator.syntax.LIST_CLOSING_BRACKET
import com.attafitamim.mtproto.generator.syntax.LIST_OPENING_BRACKET
import com.attafitamim.mtproto.generator.syntax.NAMESPACE_SEPARATOR
import com.attafitamim.mtproto.generator.syntax.SUPER_CONTAINER_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.SUPER_OBJECT_SIGNATURE
import com.attafitamim.mtproto.generator.syntax.TYPE_INDICATOR
import com.attafitamim.mtproto.generator.syntax.primitiveTypes
import com.attafitamim.mtproto.generator.utils.snakeToTitleCase

object TLTypeParser {

    fun parseGenericVariable(
        genericScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLTypeSpec.Generic.Variable {
        val name = genericScheme.substringBefore(TYPE_INDICATOR)
        val typeDescription = genericScheme.substringAfter(TYPE_INDICATOR)

        val superTypeSpec = parseType(typeDescription, genericVariables, tlContainers)
        val formattedName = name.toUpperCase()
        return TLTypeSpec.Generic.Variable(formattedName, superTypeSpec)
    }

    fun parseTLObject(
        typeScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLTypeSpec.TLType.Object {
        var namespace: String? = null
        var name: String = typeScheme

        var generics: List<TLTypeSpec.Generic>? = null
        if (name.contains(GENERIC_PARAMETER_OPENING_QUOTATION) && name.endsWith(GENERIC_PARAMETER_CLOSING_QUOTATION)) {
            generics = name.substringAfter(GENERIC_PARAMETER_OPENING_QUOTATION)
                .removeSuffix(GENERIC_PARAMETER_CLOSING_QUOTATION)
                .split(GENERIC_SEPARATOR)
                .map { genericScheme ->
                    parseGeneric(genericScheme, genericVariables, tlContainers)
                }

            name = name.substringBefore(GENERIC_PARAMETER_OPENING_QUOTATION)
        }

        if (name.contains(NAMESPACE_SEPARATOR)) {
            namespace = name.substringBeforeLast(NAMESPACE_SEPARATOR)
            name = name.substringAfterLast(NAMESPACE_SEPARATOR)
        }

        val formattedName = snakeToTitleCase(name)
        return TLTypeSpec.TLType.Object(namespace, formattedName, generics)
    }

    fun parseTLContainer(
        typeScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLTypeSpec.TLType.Container {
        var namespace: String? = null
        var name: String = typeScheme

        var generics: List<TLTypeSpec.Generic>? = null
        if (name.contains(GENERIC_PARAMETER_OPENING_QUOTATION) && name.endsWith(GENERIC_PARAMETER_CLOSING_QUOTATION)) {
            generics = name.substringAfter(GENERIC_PARAMETER_OPENING_QUOTATION)
                .removeSuffix(GENERIC_PARAMETER_CLOSING_QUOTATION)
                .split(GENERIC_SEPARATOR)
                .map { genericScheme ->
                    parseGeneric(genericScheme, genericVariables, tlContainers)
                }

            name = name.substringBefore(GENERIC_PARAMETER_OPENING_QUOTATION)
        }

        if (name.contains(NAMESPACE_SEPARATOR)) {
            namespace = name.substringBeforeLast(NAMESPACE_SEPARATOR)
            name = name.substringAfterLast(NAMESPACE_SEPARATOR)
        }

        val formattedName = snakeToTitleCase(name)
        return TLTypeSpec.TLType.Container(namespace, formattedName, generics)
    }

    fun parseType(
        typeScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLTypeSpec = when {
        // is a primitive type
        primitiveTypes.containsKey(typeScheme) -> {
            val typeClass = primitiveTypes.getValue(typeScheme)
            TLTypeSpec.Primitive(typeClass)
        }

        // is a generic variable
        !genericVariables.isNullOrEmpty() && genericVariables.containsKey(typeScheme) -> {
            genericVariables.getValue(typeScheme)
        }

        // is a Type (Any)
        typeScheme == ANY_TYPE_SIGNATURE -> TLTypeSpec.Type

        // is an Object (Any : TLObject)
        typeScheme == SUPER_OBJECT_SIGNATURE -> TLTypeSpec.TLType.SuperObject

        // is a container (Any : TLContainer)
        typeScheme == SUPER_CONTAINER_SIGNATURE -> TLTypeSpec.TLType.SuperContainer

        // is a true-flag (boolean)
        typeScheme == BOOLEAN_FLAG_SIGNATURE -> TLTypeSpec.Flag

        // is bytes with dynamic size
        typeScheme == BYTES_SIGNATURE -> TLTypeSpec.Structure.Bytes(null)

        // is byteArray with dynamic size
        typeScheme == BYTE_ARRAY_SIGNATURE -> TLTypeSpec.Structure.ByteArray(null)

        // is bytes with fixed size
        typeScheme.contains(BYTES_SIGNATURE)
                && typeScheme.contains(LIST_OPENING_BRACKET)
                && typeScheme.contains(LIST_CLOSING_BRACKET) -> {
                    val size = typeScheme.substringAfter(LIST_OPENING_BRACKET)
                        .removeSuffix(LIST_CLOSING_BRACKET)
                        .trim()
                        .takeIf(String::isNotBlank)

                    TLTypeSpec.Structure.Bytes(size)
                }

        // is byteArray with fixed size
        typeScheme.contains(BYTE_ARRAY_SIGNATURE)
                && typeScheme.contains(LIST_OPENING_BRACKET)
                && typeScheme.contains(LIST_CLOSING_BRACKET) -> {
            val size = typeScheme.substringAfter(LIST_OPENING_BRACKET)
                .removeSuffix(LIST_CLOSING_BRACKET)
                .trim()
                .takeIf(String::isNotBlank)

            TLTypeSpec.Structure.ByteArray(size)
        }

        // is a list of types
        typeScheme.startsWith(LIST_OPENING_BRACKET)
                && typeScheme.endsWith(LIST_CLOSING_BRACKET) -> {
            val genericName = typeScheme.removePrefix(LIST_OPENING_BRACKET)
                .removeSuffix(LIST_CLOSING_BRACKET)
                .trim()

            val genericSpec = parseGeneric(genericName, genericVariables, tlContainers)
            TLTypeSpec.Structure.Collection(List::class, genericSpec)
        }
        
        // extends a TLType
        else -> {
            val tlContainerType = tlContainers.firstOrNull { tlContainerSpec ->
                val fullName = buildString {
                    if (tlContainerSpec.namespace != null) {
                        append(tlContainerSpec.namespace, NAMESPACE_SEPARATOR)
                    }

                    append(tlContainerSpec.name)
                }

                fullName.equals(typeScheme, ignoreCase = true)
            }

            tlContainerType?.superType ?: parseTLObject(typeScheme, genericVariables, tlContainers)
        }
    }

    private fun parseGeneric(
        genericScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLTypeSpec.Generic {
        val genericName = genericScheme.toUpperCase()
        val genericVariable = genericVariables?.get(genericName)
        if (genericVariable != null) return genericVariable

        val typeSpec = parseType(genericScheme, genericVariables, tlContainers)
        return TLTypeSpec.Generic.Parameter(typeSpec)
    }
}
