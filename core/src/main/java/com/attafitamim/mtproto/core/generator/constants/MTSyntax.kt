package com.attafitamim.mtproto.core.generator.constants

import com.attafitamim.mtproto.core.generator.utils.titleToCamelCase
import com.attafitamim.mtproto.core.stream.MTInputStream
import com.attafitamim.mtproto.core.stream.MTOutputStream

const val PACKAGE_SEPARATOR = "."

const val TYPES_FOLDER_NAME = "types"
const val METHODS_FOLDER_NAME = "methods"

const val GLOBAL_NAMESPACE = "global"

const val TYPES_PREFIX = "MT"

const val OBJECT_NAME_SPACE_SEPARATOR = "."

const val CONSTRUCTOR_PREFIX = "#"
const val PROPERTIES_SEPARATOR = " "
const val SUPER_TYPE_PREFIX = "="
const val LINE_END = ";"

const val GENERIC_OPENING_BRACKET = "{"
const val GENERIC_CLOSING_BRACKET = "}"

const val NAMESPACE_SEPARATOR = "."

const val FLAGS_PROPERTY_NAME = "flags"
const val FLAGS_KEY_WORD = "flags:#"
const val FLAGS_DEFAULT_VALUE = 0

val INPUT_STREAM_NAME = titleToCamelCase(MTInputStream::class.java.simpleName)
val OUTPUT_STREAM_NAME = titleToCamelCase(MTOutputStream::class.java.simpleName)
