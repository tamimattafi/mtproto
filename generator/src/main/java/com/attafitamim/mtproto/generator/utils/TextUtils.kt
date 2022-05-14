package com.attafitamim.mtproto.generator.utils

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
            builder.append(c)
        }
    }

    return builder.toString()
}

fun camelToSnakeCase(string: String): String {
    val builder = StringBuilder()

    for (i in string.indices) {
        val c = string[i]
        if (c.isUpperCase()) {
            builder.append("_")
            builder.append(c.toLowerCase())
        } else if (!Character.isLetter(c) && !Character.isDigit(c)) {
            builder.append("_")
        } else {
            builder.append(c)
        }
    }

    return builder.toString()
}

fun camelToTitleCase(string: String): String {
    require(string.isNotBlank())

    val builder = StringBuilder(string)
    builder[0] = builder[0].toLowerCase()
    return builder.toString()
}

fun titleToCamelCase(string: String): String {
    require(string.isNotBlank())

    val builder = StringBuilder(string)
    builder[0] = builder[0].toLowerCase()
    return builder.toString()
}

fun snakeToTitleCase(string: String): String {
    val camelCase = snakeToCamelCase(string)
    return camelToTitleCase(camelCase)
}
