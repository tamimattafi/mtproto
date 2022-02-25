package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.generator.scheme.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*

object MTTypeParser {

    fun parseGenericVariable(
        genericScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTTypeSpec.Generic.Variable {
        val name = genericScheme.substringBefore(TYPE_INDICATOR)
        val typeDescription = genericScheme.substringAfter(TYPE_INDICATOR)

        val superTypeSpec = parseType(typeDescription, genericVariables)
        return MTTypeSpec.Generic.Variable(name, superTypeSpec)
    }

    fun parseMTObject(
        typeScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTTypeSpec.Object {
        var namespace: String? = null
        var name: String = typeScheme

        var generics: List<MTTypeSpec.Generic>? = null
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

        return MTTypeSpec.Object(namespace, name, generics)
    }

    fun parseType(
        typeScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTTypeSpec = when {
        // is a primitive type
        primitiveTypes.containsKey(typeScheme) -> {
            val typeClass = primitiveTypes.getValue(typeScheme)
            MTTypeSpec.Primitive(typeClass)
        }

        // is a generic variable
        !genericVariables.isNullOrEmpty() && genericVariables.containsKey(typeScheme) -> {
            genericVariables.getValue(typeScheme)
        }

        // is a Type (Any)
        typeScheme == ANY_TYPE_SIGNATURE -> MTTypeSpec.Type

        // is a list of types
        typeScheme.startsWith(LIST_OPENING_BRACKET) && typeScheme.endsWith(LIST_CLOSING_BRACKET) -> {
            val genericName = typeScheme.removePrefix(LIST_OPENING_BRACKET)
                .removeSuffix(LIST_CLOSING_BRACKET)
                .trim()

            val genericSpec = parseGeneric(genericName, genericVariables)
            MTTypeSpec.Structure.Collection(List::class, genericSpec)
        }

        // is an MTObject type
        else -> parseMTObject(typeScheme, genericVariables)
    }

    private fun parseGeneric(
        genericScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTTypeSpec.Generic {
        val genericVariable = genericVariables?.get(genericScheme)
        if (genericVariable != null) return genericVariable

        val typeSpec = parseType(genericScheme, genericVariables)
        return MTTypeSpec.Generic.Parameter(typeSpec)
    }
}