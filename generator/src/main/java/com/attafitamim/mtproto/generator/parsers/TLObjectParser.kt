package com.attafitamim.mtproto.generator.parsers

import com.attafitamim.mtproto.generator.types.TLObjectSpecs
import com.attafitamim.mtproto.generator.types.TLPropertySpecs
import org.gradle.api.GradleException

object TLObjectParser {

    fun parseObject(objectScheme: String): TLObjectSpecs {
        if (!isValidObjectScheme(objectScheme)) {
            throw GradleException("This TL object scheme is invalid: $objectScheme")
        }

        try {
            val name = objectScheme.substringBefore(CONSTRUCTOR_PREFIX)

            val constructorHex = objectScheme.substringAfter(CONSTRUCTOR_PREFIX).substringBefore(
                PROPERTIES_SEPARATOR
            )
            val constructor = constructorHex.toLong(16).toInt()


            val hasFlags = objectScheme.contains(FLAGS_KEY_WORD)

            val propertiesStringPrefix = if (hasFlags) FLAGS_KEY_WORD else PROPERTIES_SEPARATOR
            val propertiesString = objectScheme.substringAfter(propertiesStringPrefix).substringBefore(
                SUPER_TYPE_PREFIX
            ).trim()

            var tlPropertySpecs: List<TLPropertySpecs>? = null
            if (propertiesString.isNotBlank()) {
                val properties = propertiesString.split(PROPERTIES_SEPARATOR)
                tlPropertySpecs = properties.map(TLPropertyParser::parse)
            }

            val superType = objectScheme.substringAfter(SUPER_TYPE_PREFIX).substringBefore(LINE_END).trim()

            return TLObjectSpecs(
                    objectScheme,
                    name,
                    superType,
                    constructor,
                    hasFlags,
                    tlPropertySpecs
            )
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to parse object scheme
                        Scheme: $objectScheme
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    //TODO: optimize this and use regular expressions if possible
    fun isValidObjectScheme(objectScheme: String): Boolean {
        try {
            val isNotComment = !objectScheme.startsWith("/")
            val hasLineEnd = objectScheme.endsWith(LINE_END)
            val hasConstructor = objectScheme.contains(CONSTRUCTOR_PREFIX)
            val hasSuperTypePrefix = objectScheme.contains(SUPER_TYPE_PREFIX)

            return hasLineEnd && hasConstructor && hasSuperTypePrefix && isNotComment
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to validate object scheme
                        Scheme: $objectScheme
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    const val FLAGS_KEY_WORD = "flags:#"
    const val CONSTRUCTOR_PREFIX = "#"
    const val PROPERTIES_SEPARATOR = " "
    const val SUPER_TYPE_PREFIX = "="
    const val LINE_END = ";"

}