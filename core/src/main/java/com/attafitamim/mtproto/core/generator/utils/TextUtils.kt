package com.attafitamim.mtproto.core.generator.utils

fun snakeToCamelCase(string: String): String {
    var hadWhiteSpace = false
    val builder = StringBuilder()

    for (i in string.indices) {
        val c = string[i]
        if (hadWhiteSpace) {
            if (Character.isLetter(c) || Character.isDigit(c)) {
                builder.append(Character.toUpperCase(c))
                hadWhiteSpace = false
            }
        } else if (!Character.isLetter(c) && !Character.isDigit(c)) {
            hadWhiteSpace = true
        } else {
            builder.append(Character.toLowerCase(c))
        }
    }

    return builder.toString()
}

fun camelToTitleCase(string: String): String {
    require(string.isNotBlank())

    val builder = StringBuilder(string)
    builder[0] = builder[0].uppercaseChar()
    return builder.toString()
}

fun titleToCamelCase(string: String): String {
    require(string.isNotBlank())

    val builder = StringBuilder(string)
    builder[0] = builder[0].lowercaseChar()
    return builder.toString()
}

fun snakeToTitleCase(string: String): String {
    val camelCase = snakeToCamelCase(string)
    return camelToTitleCase(camelCase)
}