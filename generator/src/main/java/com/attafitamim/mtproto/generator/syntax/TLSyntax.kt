package com.attafitamim.mtproto.generator.syntax

const val TYPES_KEYWORD = "---types---"
const val FUNCTIONS_KEYWORD = "---functions---"

const val TYPES_FOLDER_NAME = "types"
const val CONTAINERS_FOLDER_NAME = "containers"
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
const val METHOD_RESPONSE_NAME = "response"

const val ANY_TYPE_SIGNATURE = "Type"
const val SUPER_OBJECT_SIGNATURE = "Object"
const val SUPER_CONTAINER_SIGNATURE = "Container"

const val BOOLEAN_FLAG_SIGNATURE = "true"
const val BYTES_SIGNATURE = "bytes"

const val FLAG_INITIAL_VALUE = 0
const val FLAG_BASE = 2.0

val primitiveTypes = mapOf(
    "byte" to Byte::class,
    "string" to String::class,
    "int" to Int::class,
    "long" to Long::class,
    "boolean" to Boolean::class,
    "bool" to Boolean::class,
    "double" to Double::class
)
