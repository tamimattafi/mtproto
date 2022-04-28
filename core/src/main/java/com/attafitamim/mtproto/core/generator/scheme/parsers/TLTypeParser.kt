package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase

object TLTypeParser {

    fun parseGenericVariable(
        genericScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
    ): TLTypeSpec.Generic.Variable {
        val name = genericScheme.substringBefore(TYPE_INDICATOR)
        val typeDescription = genericScheme.substringAfter(TYPE_INDICATOR)

        val superTypeSpec = parseType(typeDescription, genericVariables)
        val formattedName = name.uppercase()
        return TLTypeSpec.Generic.Variable(formattedName, superTypeSpec)
    }

    fun parseMTObject(
        typeScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
    ): TLTypeSpec.Object {
        var namespace: String? = null
        var name: String = typeScheme

        var generics: List<TLTypeSpec.Generic>? = null
        if (name.contains(GENERIC_PARAMETER_OPENING_QUOTATION) && name.endsWith(GENERIC_PARAMETER_CLOSING_QUOTATION)) {
            generics = name.substringAfter(GENERIC_PARAMETER_OPENING_QUOTATION)
                .removeSuffix(GENERIC_PARAMETER_CLOSING_QUOTATION)
                .split(GENERIC_SEPARATOR)
                .map { genericScheme ->
                    parseGeneric(genericScheme, genericVariables)
                }

            name = name.substringBefore(GENERIC_PARAMETER_OPENING_QUOTATION)
        }

        if (name.contains(NAMESPACE_SEPARATOR)) {
            namespace = name.substringBeforeLast(NAMESPACE_SEPARATOR)
            name = name.substringAfterLast(NAMESPACE_SEPARATOR)
        }

        val formattedName = snakeToTitleCase(name)
        return TLTypeSpec.Object(namespace, formattedName, generics)
    }

    fun parseType(
        typeScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
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

        // is a true-flag (boolean)
        typeScheme == FLAG_SIGNATURE -> TLTypeSpec.Flag

        // is a list of types
        typeScheme.startsWith(LIST_OPENING_BRACKET) && typeScheme.endsWith(LIST_CLOSING_BRACKET) -> {
            val genericName = typeScheme.removePrefix(LIST_OPENING_BRACKET)
                .removeSuffix(LIST_CLOSING_BRACKET)
                .trim()

            val genericSpec = parseGeneric(genericName, genericVariables)
            TLTypeSpec.Structure.Collection(List::class, genericSpec)
        }

        // is an MTObject type
        else -> parseMTObject(typeScheme, genericVariables)
    }

    private fun parseGeneric(
        genericScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
    ): TLTypeSpec.Generic {
        val genericName = genericScheme.uppercase()
        val genericVariable = genericVariables?.get(genericName)
        if (genericVariable != null) return genericVariable

        val typeSpec = parseType(genericScheme, genericVariables)
        return TLTypeSpec.Generic.Parameter(typeSpec)
    }
}