package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.exceptions.TLSchemeParseException
import com.attafitamim.mtproto.core.generator.scheme.specs.*
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase

object TLMethodParser {

    fun parseMethod(
        objectScheme: String,
        tlContainers: List<TLContainerSpec>
    ): TLMethodSpec {
        if (!isValidMethodScheme(objectScheme)) {
            throw TLSchemeParseException(
                objectScheme,
                "Invalid method scheme"
            )
        }

        try {
            val hasGenerics = objectScheme.contains(GENERIC_VARIABLE_OPENING_BRACKET) &&
                    objectScheme.contains(GENERIC_VARIABLE_CLOSING_BRACKET)

            val hasFlags = objectScheme.contains(FLAGS_KEY_WORD)

            val propertiesStringPrefix = when {
                hasFlags -> FLAGS_KEY_WORD
                hasGenerics -> GENERIC_VARIABLE_CLOSING_BRACKET
                else -> PROPERTIES_SEPARATOR
            }

            val typeStringPostfix = when {
                hasGenerics -> GENERIC_VARIABLE_OPENING_BRACKET
                hasFlags -> FLAGS_KEY_WORD
                else -> PROPERTIES_SEPARATOR
            }

            var name = objectScheme.substringBefore(typeStringPostfix)
                .substringBefore(CONSTRUCTOR_PREFIX)

            val constructorHash = objectScheme.substringBefore(typeStringPostfix)
                .substringAfter(CONSTRUCTOR_PREFIX, missingDelimiterValue = "")
                .trim()

            var namespace: String? = null
            if (name.contains(NAMESPACE_SEPARATOR)) {
                namespace = name.substringBeforeLast(NAMESPACE_SEPARATOR)
                name = name.substringAfterLast(NAMESPACE_SEPARATOR)
            }

            var genericVariables: HashMap<String, TLTypeSpec.Generic.Variable>? = null
            if (hasGenerics) {
                genericVariables = HashMap()
                objectScheme.substringAfter(GENERIC_VARIABLE_OPENING_BRACKET)
                    .substringBefore(GENERIC_VARIABLE_CLOSING_BRACKET)
                    .split(GENERIC_SEPARATOR)
                    .forEach { genericScheme ->
                        val variable = TLTypeParser.parseGenericVariable(
                            genericScheme,
                            genericVariables,
                            tlContainers
                        )

                        genericVariables[variable.name] = variable
                    }
            }

            val propertiesString = objectScheme.substringAfter(propertiesStringPrefix)
                .substringBefore(SUPER_TYPE_PREFIX)
                .trim()

            var tlPropertySpecs: List<TLPropertySpec>? = null
            if (propertiesString.isNotBlank()) {
                tlPropertySpecs = propertiesString.split(PROPERTIES_SEPARATOR)
                    .map { propertyScheme ->
                        TLPropertyParser.parseProperty(
                            propertyScheme,
                            genericVariables,
                            tlContainers
                        )
                    }
            }

            val superType = objectScheme.substringAfter(SUPER_TYPE_PREFIX)
                .substringBefore(LINE_END)
                .trim()

            val superTypeSpec = TLTypeParser.parseType(
                superType,
                genericVariables,
                tlContainers
            )
            
            val formattedName = snakeToTitleCase(name)
            return TLMethodSpec(
                objectScheme,
                formattedName,
                namespace,
                superTypeSpec,
                constructorHash,
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
    fun isValidMethodScheme(objectScheme: String): Boolean {
        return TLObjectParser.isValidObjectScheme(objectScheme)
    }
}