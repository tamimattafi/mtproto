package com.attafitamim.mtproto.core.generator.syntax

const val PACKAGE_SEPARATOR = "."

const val TYPES_FOLDER_NAME = "types"
const val METHODS_FOLDER_NAME = "methods"

const val GLOBAL_NAMESPACE = "global"

const val TYPES_PREFIX = "TL"

const val CONSTRUCTOR_PREFIX = "#"
const val PROPERTIES_SEPARATOR = " "
const val SUPER_TYPE_PREFIX = "="
const val LINE_END = ";"

const val GENERIC_VARIABLE_OPENING_BRACKET = "{"
const val GENERIC_VARIABLE_CLOSING_BRACKET = "}"
const val LIST_OPENING_BRACKET = "["
const val LIST_CLOSING_BRACKET = "]"

const val GENERIC_PARAMETER_OPENING_QUOTATION = "<"
const val GENERIC_PARAMETER_CLOSING_QUOTATION = ">"
const val TYPE_INDICATOR = ":"
const val GENERIC_SEPARATOR = ","

const val NAMESPACE_SEPARATOR = "."

const val FLAGS_PROPERTY_NAME = "flags"
const val FLAGS_KEY_WORD = "flags:#"
const val FLAGS_DEFAULT_VALUE = 0

const val PROPERTY_FLAG_PREFIX = "flags."
const val PROPERTY_FLAG_SEPARATOR = "?"

const val INPUT_STREAM_NAME = "inputStream"
const val OUTPUT_STREAM_NAME = "outputStream"
const val ARRAY_ELEMENT_NAME = "element"

const val ANY_TYPE_SIGNATURE = "Type"
const val FLAG_SIGNATURE = "true"

const val DEFAULT_FLAG_VALUE = 0
const val DEFAULT_FLAG_BASE = 2.0

val primitiveTypes = mapOf(
    "string" to String::class,
    "int" to Int::class,
    "long" to Long::class,
    "boolean" to Boolean::class,
    "bool" to Boolean::class,
    "double" to Double::class,
    "bytes" to ByteArray::class
)