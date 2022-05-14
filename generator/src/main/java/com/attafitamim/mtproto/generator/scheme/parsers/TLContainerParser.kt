package com.attafitamim.mtproto.generator.scheme.parsers

import com.attafitamim.mtproto.generator.exceptions.TLSchemeParseException
import com.attafitamim.mtproto.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.generator.syntax.*
import com.attafitamim.mtproto.generator.utils.snakeToTitleCase

object TLContainerParser {

    fun parseContainer(
        objectScheme: String,
        tlContainers: List<TLContainerSpec>
    ): TLContainerSpec {
        if (!isValidContainerScheme(objectScheme)) {
            throw TLSchemeParseException(
                objectScheme,
                "Invalid container scheme"
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

            if (propertiesString.isBlank()) throw TLSchemeParseException(
                objectScheme,
                "Containers must have at least on property"
            )

            val tlPropertySpecs = propertiesString.split(PROPERTIES_SEPARATOR)
                .map { propertyScheme ->
                    TLPropertyParser.parseProperty(
                        propertyScheme,
                        genericVariables,
                        tlContainers
                    )
                }

            val superType = objectScheme.substringAfter(SUPER_TYPE_PREFIX)
                .substringBefore(LINE_END)
                .trim()

            val superTypeSpec = TLTypeParser.parseTLContainer(
                superType,
                genericVariables,
                tlContainers
            )
            
            val formattedName = snakeToTitleCase(name)
            return TLContainerSpec(
                objectScheme,
                formattedName,
                namespace,
                superTypeSpec,
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
    fun isValidContainerScheme(objectScheme: String): Boolean {
        val isNotComment = !objectScheme.startsWith("/")
        val hasLineEnd = objectScheme.endsWith(LINE_END)
        val hasSuperTypePrefix = objectScheme.contains(SUPER_TYPE_PREFIX)

        return hasLineEnd && hasSuperTypePrefix && isNotComment
    }
}
