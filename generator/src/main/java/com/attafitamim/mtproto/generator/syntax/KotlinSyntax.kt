package com.attafitamim.mtproto.generator.syntax

import com.attafitamim.mtproto.generator.plugin.TLGeneratorPlugin

// Separators
const val KEYWORD_SEPARATOR = " "
const val PARAMETER_SEPARATOR = ", "
const val PACKAGE_SEPARATOR = "."

// Access
const val INSTANCE_ACCESS_KEY = "."
const val CLASS_ACCESS_KEY = "::"

// Control flow
const val PARAMETER_OPEN_PARENTHESIS = "("
const val PARAMETER_CLOSE_PARENTHESIS = ")"
const val CURLY_BRACE_OPEN = "{"
const val CURLY_BRACE_CLOSE = "}"
const val RESULT_ARROW = " -> "
const val INITIALIZATION_SIGN = " = "
const val EQUAL_SIGN = " == "
const val NOT_EQUAL_SIGN = " != "
const val LESS_THAN_SIGN = " < "
const val TYPE_SIGN = ":"
const val NULLABLE_SIGN = "?"
const val CONSTANT_NAME_SEPARATOR = "_"
const val NEW_LINE = "\n"

// Keywords
const val RETURN_KEYWORD = "return"
const val WHEN_KEYWORD = "when"
const val IF_KEYWORD = "if"
const val ELSE_KEYWORD = "else"
const val THROW_KEYWORD = "throw"
const val CLASS_KEYWORD = "class"
const val VARIABLE_KEYWORD = "var"
const val IMMUTABLE_KEYWORD = "val"
const val NULL_KEYWORD = "null"
const val IT_KEYWORD = "it"
const val WHILE_KEYWORD = "while"
const val INVOKE_KEYWORD = "invoke"

const val AND_KEYWORD = " and "
const val OR_KEYWORD = " or "

const val INVERT_METHOD = "inv()"
const val REQUIRE_METHOD = "require"
const val FOR_EACH_METHOD = "forEach"

const val COMPANION_DEFAULT_NAME = "Companion"
