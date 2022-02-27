package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.generator.scheme.specs.MTPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.MTTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase

object MTPropertyParser {

    fun parseProperty(
        propertyScheme: String,
        genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
    ): MTPropertySpec {
        val name = propertyScheme.substringBefore(TYPE_INDICATOR)
        val typeDescription = propertyScheme.substringAfter(TYPE_INDICATOR)

        var flag: Int? = null
        val type: String
        if (typeDescription.contains(PROPERTY_FLAG_SEPARATOR)) {
            flag = parseTypeFlag(typeDescription)
            type = typeDescription.substringAfter(PROPERTY_FLAG_SEPARATOR)
        } else {
            type = typeDescription
        }

        val propertyTypeSpec = MTTypeParser.parseType(type, genericVariables)
        val formattedName = snakeToCamelCase(name)
        return MTPropertySpec(propertyScheme, formattedName, flag, propertyTypeSpec)
    }

    private fun parseTypeFlag(typeString: String): Int {
        val flagString = typeString.substringAfter(PROPERTY_FLAG_PREFIX)
            .substringBefore(PROPERTY_FLAG_SEPARATOR)

        return flagString.toInt()
    }
}