package com.attafitamim.mtproto.core.generator.parsers

import com.attafitamim.mtproto.core.generator.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.specs.MTTypeSpec

object MTPropertyParser {

    private const val PROPERTY_FLAG_PREFIX = "flags."
    private const val PROPERTY_FLAG_SEPARATOR = "?"
    private const val PROPERTY_TYPE_SEPARATOR = ":"

    fun parseProperty(
        propertyScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTPropertySpec {
        val name = propertyScheme.substringBefore(PROPERTY_TYPE_SEPARATOR)
        val typeDescription = propertyScheme.substringAfter(PROPERTY_TYPE_SEPARATOR)

        var flag: Int? = null
        val type: String
        if (typeDescription.contains(PROPERTY_FLAG_SEPARATOR)) {
            flag = parseTypeFlag(typeDescription)
            type = typeDescription.substringAfter(PROPERTY_FLAG_SEPARATOR)
        } else {
            type = typeDescription
        }

        val propertyTypeSpec = MTTypeParser.parseType(type, genericVariables)
        return MTPropertySpec(propertyScheme, name, flag, propertyTypeSpec)
    }

    private fun parseTypeFlag(typeString: String): Int {
        val flagString = typeString.substringAfter(PROPERTY_FLAG_PREFIX).substringBefore(
            PROPERTY_FLAG_SEPARATOR
        )
        return flagString.toInt()
    }
}