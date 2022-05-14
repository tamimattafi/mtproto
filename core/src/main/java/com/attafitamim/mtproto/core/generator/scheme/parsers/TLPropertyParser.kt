package com.attafitamim.mtproto.core.generator.scheme.parsers

import com.attafitamim.mtproto.core.generator.scheme.specs.TLContainerSpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLPropertySpec
import com.attafitamim.mtproto.core.generator.scheme.specs.TLTypeSpec
import com.attafitamim.mtproto.core.generator.syntax.*
import com.attafitamim.mtproto.core.generator.utils.snakeToCamelCase
import com.attafitamim.mtproto.core.generator.utils.snakeToTitleCase

object TLPropertyParser {

    fun parseProperty(
        propertyScheme: String,
        genericVariables: Map<String, TLTypeSpec.Generic.Variable>?,
        tlContainers: List<TLContainerSpec>
    ): TLPropertySpec {
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

        val propertyTypeSpec = TLTypeParser.parseType(
            type,
            genericVariables,
            tlContainers
        )

        val formmatedName = snakeToCamelCase(name)
        return TLPropertySpec(propertyScheme, formmatedName, flag, propertyTypeSpec)
    }

    private fun parseTypeFlag(typeString: String): Int {
        val flagString = typeString.substringAfter(PROPERTY_FLAG_PREFIX)
            .substringBefore(PROPERTY_FLAG_SEPARATOR)

        return flagString.toInt()
    }
}
