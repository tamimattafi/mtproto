package com.attafitamim.mtproto.core.generator.classes

import org.gradle.api.GradleException

object TextUtils {

    fun snakeToCamelCase(string: String): String {
        try {
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
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to convert string from SnakeCase to CamelCase
                        String: $string
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun camelToTitleCase(string: String): String {
        try {
            require(string.isNotBlank())

            val builder = StringBuilder(string)
            builder[0] = builder[0].toUpperCase()
            return builder.toString()
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to convert string from CamelCase to TitleCase
                        String: $string
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

}