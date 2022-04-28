package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.exceptions.TLSchemeParseException
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.scheme.specs.TLObjectSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase
import java.lang.Exception

object TLObjectParser {

    fun parseObject(objectScheme: String): TLObjectSpec {

        if (!isValidObjectScheme(objectScheme)) {
            throw TLSchemeParseException(
                objectScheme,
                "Invalid object scheme"
            )
        }

        try {
            var name = objectScheme.substringBefore(CONSTRUCTOR_PREFIX)
            var namespace: String? = null
            if (name.contains(NAMESPACE_SEPARATOR)) {
                namespace = name.substringBeforeLast(NAMESPACE_SEPARATOR)
                name = name.substringAfterLast(NAMESPACE_SEPARATOR)
            }

            val constructorHex = objectScheme.substringAfter(CONSTRUCTOR_PREFIX)
                .substringBefore(PROPERTIES_SEPARATOR)

            val hash = constructorHex.toLong(16).toInt()

            var genericVariables: HashMap<String, TLTypeSpec.Generic.Variable>? = null
            val hasGenerics = objectScheme.contains(GENERIC_VARIABLE_OPENING_BRACKET) &&
                    objectScheme.contains(GENERIC_VARIABLE_CLOSING_BRACKET)

            if (hasGenerics) {
                genericVariables = HashMap()
                objectScheme.substringAfter(GENERIC_VARIABLE_OPENING_BRACKET)
                    .substringBefore(GENERIC_VARIABLE_CLOSING_BRACKET)
                    .split(GENERIC_SEPARATOR)
                    .forEach { genericScheme ->
                        val variable = TLTypeParser.parseGenericVariable(
                            genericScheme,
                            genericVariables
                        )

                        genericVariables[variable.name] = variable
                    }
            }

            val hasFlags = objectScheme.contains(FLAGS_KEY_WORD)

            val propertiesStringPrefix = when {
                hasFlags -> FLAGS_KEY_WORD
                hasGenerics -> GENERIC_VARIABLE_CLOSING_BRACKET
                else -> PROPERTIES_SEPARATOR
            }

            val propertiesString = objectScheme.substringAfter(propertiesStringPrefix)
                .substringBefore(SUPER_TYPE_PREFIX)
                .trim()

            var tlPropertySpecs: List<TLPropertySpec>? = null
            if (propertiesString.isNotBlank()) {
                tlPropertySpecs = propertiesString.split(PROPERTIES_SEPARATOR)
                    .map { propertyScheme ->
                        TLPropertyParser.parseProperty(propertyScheme, genericVariables)
                    }
            }

            val superType = objectScheme.substringAfter(SUPER_TYPE_PREFIX)
                .substringBefore(LINE_END)
                .trim()

            val superTypeSpec = TLTypeParser.parseMTObject(superType, genericVariables)
            val formattedName = snakeToTitleCase(name)

            return TLObjectSpec(
                objectScheme,
                formattedName,
                namespace,
                superTypeSpec,
                hash,
                hasFlags,
                tlPropertySpecs,
                genericVariables
            )
        } catch (e: Exception) {
            throw TLSchemeParseException(
                objectScheme,
                e.toString()
            )
        }
    }

    //TODO: optimize this and use regular expressions if possible
    fun isValidObjectScheme(objectScheme: String): Boolean {
        val isNotComment = !objectScheme.startsWith("/")
        val hasLineEnd = objectScheme.endsWith(LINE_END)
        val hasConstructor = objectScheme.contains(CONSTRUCTOR_PREFIX)
        val hasSuperTypePrefix = objectScheme.contains(SUPER_TYPE_PREFIX)

        return hasLineEnd && hasConstructor && hasSuperTypePrefix && isNotComment
    }
}