package com.attafitamim.mtproto.generator.parsers

import com.attafitamim.mtproto.generator.types.TLPropertySpecs
import org.gradle.api.GradleException

object TLPropertyParser {

    fun parse(propertyScheme: String): TLPropertySpecs {
        try {
            val name = propertyScheme.substringBefore(PROPERTY_TYPE_SEPARATOR)
            val typeString = propertyScheme.substringAfter(PROPERTY_TYPE_SEPARATOR)
            var flag: Int? = null
            val type: String
            if (typeString.contains(PROPERTY_FLAG_SEPARATOR)) {
                flag = parseTypeFlag(typeString)
                type = typeString.substringAfter(PROPERTY_FLAG_SEPARATOR)
            } else {
                type = typeString
            }

            return TLPropertySpecs(propertyScheme, name, type, flag)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to parse object property
                        Property Scheme: $propertyScheme
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    private fun parseTypeFlag(typeString: String): Int {
        try {
            val flagString = typeString.substringAfter(PROPERTY_FLAG_PREFIX).substringBefore(
                PROPERTY_FLAG_SEPARATOR
            )
            return flagString.toInt()
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to parse property flag
                        Type String: $typeString
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    private const val PROPERTY_FLAG_PREFIX = "flags."
    private const val PROPERTY_FLAG_SEPARATOR = "?"
    private const val PROPERTY_TYPE_SEPARATOR = ":"

}